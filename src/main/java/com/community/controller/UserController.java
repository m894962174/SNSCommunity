package com.community.controller;

import com.community.annotation.CheckLogin;
import com.community.service.impl.UserService;
import com.community.util.CommonUtil;
import com.community.util.UserThreadLocal;
import com.community.vo.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/16/10:18
 * @Description:
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;


    @CheckLogin
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    /**
     * 上传头像
     *
     * @param model
     * @param multipartFile
     * @return
     */
    @CheckLogin
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String uploadFile(Model model, MultipartFile multipartFile) {
        if (multipartFile == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }
        String fileName = multipartFile.getOriginalFilename();
        String suffix = fileName.substring(fileName.indexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确!");
            return "/site/setting";
        }
        //将上传的文件存到服务器内
        fileName = CommonUtil.generateUUID() + suffix;
        File file = new File(uploadPath + "/" + fileName);
        try {
            multipartFile.transferTo(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //更改User的headUrl
        User user = UserThreadLocal.getUser();
        user.setHeaderUrl(domain + contextPath + "/user/header/" + fileName);
        userService.updateById(user);
        return "redirect:/index";
    }

    /**
     * 导航栏的头像显示
     *
     * @param fileName
     * @param response
     */
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器的头像存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (FileInputStream fis = new FileInputStream(new File(fileName));
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            while (fis.read(buffer) != -1) {
                outputStream.write(buffer);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     *
     * @param model
     * @param OldPassWord
     * @param passWord
     * @return
     */
    @CheckLogin
    @RequestMapping(value = "/changePassWord", method = RequestMethod.POST)
    public String changePassWord(Model model, String OldPassWord, String passWord, String checkPassWord) {
        Map<String, Object> map = userService.updatePassWord(OldPassWord, passWord, checkPassWord);
        if (map == null) {
            return "redirect:/index";
        }
        model.addAttribute("errorMsg", map.get("errorMsg"));
        model.addAttribute("checkMsg", map.get("checkMsg"));
        return "/site/setting";
    }
}
