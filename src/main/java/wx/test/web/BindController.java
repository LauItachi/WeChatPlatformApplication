package wx.test.web;

import com.mxixm.fastboot.weixin.module.user.WxUser;
import com.mxixm.fastboot.weixin.util.WxWebUtils;
import com.mxixm.fastboot.weixin.web.WxUserManager;
import com.mxixm.fastboot.weixin.web.WxWebUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import wx.test.model.Student;
import wx.test.service.StudentService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by MagikLau on 2018/1/29.
 * Java web dev
 */
@Controller
@RequestMapping("/wx/bind")
public class BindController {

    @Autowired
    private WxUserManager wxUserManager ;

    @Autowired
    private StudentService studentService;


    @RequestMapping("")
    public String bind(ModelMap modelMap) {

        WxWebUser wxWebUser = WxWebUtils.getWxWebUserFromSession();
        WxUser wxUser = wxUserManager.getWxUserByWxWebUser(wxWebUser);
        String openID = wxWebUser.getOpenId();
        modelMap.addAttribute("openID", openID);
        System.out.println("openID:"+openID);
        Student student = studentService.findStudentByOpenID(openID);
        String result ;
        if( student != null ){
            System.out.println(student);
            Integer studentID = student.getStudentID();
            modelMap.addAttribute("studentID", studentID);
            System.out.println("studentID:"+student.getStudentID());

            result = "bind_confirm";
        }else{
            String nickName = wxUser.getNickName();
            modelMap.addAttribute("nickName", nickName);
            System.out.println("nickName:"+nickName);

            result = "bind";
        }

        return result;
    }

    @RequestMapping("/check")/*(value = "/wx/bind/check", method = RequestMethod.POST)*/
    public void bindCheck(ModelMap modelMap, HttpServletResponse httpServletResponse,
                                          String openID, Integer studentID, String last6ID) throws IOException {
        httpServletResponse.setContentType("application/json;charset=utf-8");

        Boolean checked = studentService.checkId(studentID, last6ID);
        System.out.println("checked:"+checked);
        String data ;
        if( checked ){
            studentService.bindOpenID(studentID, openID);
            modelMap.addAttribute("studentID", studentID);
            data = "{\"status\":\"ok\",\"msg\":\"绑定成功\"}";
        }else{
            data = "{\"status\":\"error\",\"msg\":\"绑定失败\"}";
        }
        PrintWriter out = httpServletResponse.getWriter();
        out.write(data);
        out.flush();
        out.close();
    }

    @RequestMapping("/confirm")/*(value = "/wx/bind/confirm", method = RequestMethod.POST)*/
    public String bindConfirm(ModelMap modelMap, Integer studentID) {
        String result ;
        if( studentID != null ){
            modelMap.addAttribute("studentID", studentID);
            result = "bind_confirm";
        }else{
            String msg = "验证有误.";
            modelMap.addAttribute("msg", msg);
            result = "redirect:";
        }
        return result;
    }


}
