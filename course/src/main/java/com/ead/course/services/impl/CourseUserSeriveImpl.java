package com.ead.course.services.impl;

import com.ead.course.repositories.CourseUserRepository;
import com.ead.course.services.CourseUserSerive;
import org.springframework.beans.factory.annotation.Autowired;

public class CourseUserSeriveImpl implements CourseUserSerive {

    @Autowired
    CourseUserRepository courseUserRepository;
}
