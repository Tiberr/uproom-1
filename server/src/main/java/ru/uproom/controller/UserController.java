package ru.uproom.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.uproom.domain.User;
import ru.uproom.prsistence.UserDao;
import ru.uproom.service.DeviceStorageService;
import ru.uproom.service.SessionHolder;
import ru.uproom.service.SessionHolderImpl;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by HEDIN on 12.07.2014.
 * todo hash pass
 */
@Controller
@RequestMapping(value = "user")
public class UserController {
    @Autowired
    private UserDao userDao;

    @Autowired
    private DeviceStorageService deviceStorageService;


    private SessionHolder sessionHolder = SessionHolderImpl.getInstance();

    @RequestMapping(method = RequestMethod.GET, value = "list")
    @ResponseBody
    public List<User> listUsers() {
        return userDao.listUsers();
    }

    @RequestMapping(method = RequestMethod.GET, value = "register")
    @ResponseBody
    public String register(@RequestParam String username,
                           @RequestParam String password) {
        userDao.saveNewUser(new User(username, password));
        return "ok";
    }

    @RequestMapping(method = RequestMethod.GET, value = "login")
    @ResponseBody
    public User login(@RequestParam String username,
                      @RequestParam String password,
                      HttpServletRequest request) {
        User user = userDao.authenticate(username, password);
        if (null == user)
            return new User();
        String sid = request.getRemoteHost() + System.currentTimeMillis() + Math.random();
        sessionHolder.newSession(sid, user);
        deviceStorageService.onNewUser(user.getId());
        request.getSession().setAttribute("SID",sid);
        return user;
    }

    @RequestMapping(method = RequestMethod.GET, value = "logout")
    @ResponseBody
    public String logout() {
        return "ok";
    }

}
