package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.ead.authuser.specifications.SpecificationTemplate;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
@RestController
@CrossOrigin(value = "*", maxAge = 3600)
@RequestMapping("/users")
@Tag(name = "Usuários", description = "Informações sobre os usuários")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "Listar todos usuários ou por curso", description = "Essa função é responsável por listar por usuários")
    public ResponseEntity<Page<UserModel>> getAllUsers(SpecificationTemplate.UserPec spec,
                                                        @PageableDefault(page = 0, size = 10, sort = "userId",
                                                        direction = Sort.Direction.ASC) @ParameterObject Pageable pageable,
                                                        @RequestParam(required = false) UUID courseId) {
        Page<UserModel> userModelPage = null;
        if(courseId != null) {
            userModelPage = userService.findAll(SpecificationTemplate.userCourseId(courseId).and(spec), pageable);
        } else {
            userModelPage = userService.findAll(spec, pageable);
        }
        if(!userModelPage.isEmpty()) {
            for(UserModel user: userModelPage.toList()) {
                user.add(linkTo(methodOn(UserController.class).getOneUser(user.getUserId())).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(userModelPage);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Listar todos usuários por uuid", description = "Essa função é responsável por listar por usuários por uuid")
    public ResponseEntity<Object> getOneUser(@PathVariable(value = "userId") UUID userId) {
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (!userModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(userModelOptional.get());
        }
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Deletar usuários", description = "Essa função é responsável por deletar usuários")
    public ResponseEntity<Object> deleteUser(@PathVariable(value = "userId") UUID userId){
        log.debug("DELETE deleteUser userId received {} ", userId);
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (!userModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } else {
            userService.delete(userModelOptional.get());
            log.debug("DELETE deleteUser userId saved {} ", userId);
            log.info("User deleted successfully userId {} ", userId);
            return ResponseEntity.status(HttpStatus.OK).body("User deleted success.");
        }
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Atualizar usuários", description = "Essa função é responsável por atualizar usuários por id")
    public ResponseEntity<Object> updateUser(@PathVariable(value = "userId") UUID userId,
                                             @RequestBody @Validated(UserDto.UserView.UserPut.class)
                                             @JsonView(UserDto.UserView.UserPut.class) UserDto userDto){
        log.debug("PUT updateUser userDto received {} ", userDto.toString());
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (!userModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } else {
            var userModel = userModelOptional.get();
            userModel.setFullName(userDto.getFullName());
            userModel.setPhoneNumber(userDto.getPhoneNumber());
            userModel.setCpf(userDto.getCpf());
            userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
            userService.save(userModel);
            log.debug("PUT updateUser userId saved {} ", userModel.getUserId());
            log.info("User updated successfully userId {} ", userModel.getUserId());
            return ResponseEntity.status(HttpStatus.OK).body(userModel);
        }
    }

    @PutMapping("/{userId}/password")
    @Operation(summary = "Atualizar senha", description = "Essa função é responsável por atualizar senha")
    public ResponseEntity<Object> updatePassword(@PathVariable(value = "userId") UUID userId,
                                                 @RequestBody @Validated(UserDto.UserView.PasswordPut.class)
                                                 @JsonView(UserDto.UserView.PasswordPut.class) UserDto userDto){
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (!userModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } if(!userModelOptional.get().getPassword().equals(userDto.getOldPassword())){
            log.warn("Mismatched old password userID {} ", userDto.getUsername());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Mismatched old password!");
        }
        else {
            var userModel = userModelOptional.get();
            userModel.setPassword(userDto.getPassword());
            userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
            userService.save(userModel);
            log.debug("PUT updatePassword userId saved {} ", userModel.getUserId());
            log.info("Password updated successfully userId {} ", userModel.getUserId());
            return ResponseEntity.status(HttpStatus.OK).body("Password updated successfully.");
        }
    }

    @PutMapping("/{userId}/image")
    @Operation(summary = "Atualizar imagem", description = "Essa função é responsável por atualizar imagem")
    public ResponseEntity<Object> updateImage(@PathVariable(value = "userId") UUID userId,
                                              @RequestBody @Validated(UserDto.UserView.ImagePut.class)
                                              @JsonView(UserDto.UserView.ImagePut.class) UserDto userDto){
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (!userModelOptional.isPresent()) {
            log.warn("User not found. {} ", userDto.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }else {
            var userModel = userModelOptional.get();
            userModel.setImageUrl(userDto.getImageUrl());
            userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
            userService.save(userModel);
            log.debug("PUT updateImage userId saved {} ", userModel.getUserId());
            log.info("Image updated successfully userId {} ", userModel.getUserId());
            return ResponseEntity.status(HttpStatus.OK).body(userModel);
        }
    }



}