package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * */
    @PostMapping("login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        /**
         * 1.没有查询到员工账户返回失败
         * 2.密码比对：不一致返回登录失败
         * 3.查看员工状态，如果为已禁用返回登录失败
         * 4.登录成功将员工id存入Session并返回登录成功
         * */
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        //数据库没有相应的员工账户信息 返回登录失败
        if(emp==null){
            return R.error("登录失败");
        }
        //密码比对
        if(!emp.getPassword().equals(password)){
            return R.error("账号密码错误");
        }
        //查看员工状态
        if(emp.getStatus() == 0){
            return R.error("账号已禁用");
        }
        //登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }
    /**
     * 员工退出登录
     * */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }
    /**
     * 新增员工
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
//        设置初始密码123456 并通过md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //employee.setCreateTime(LocalDateTime.now()); 已弃用:在MyMetaObjecthandler类自动填充
        //employee.setUpdateTime(LocalDateTime.now());
//        获得当前登录用户的id
        //已弃用:在MyMetaObjecthandler类自动填充
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
        employeeService.save(employee);
        return R.success("新增员工成功");
    }
    /**
     * 员工信息分页查询
     * */
    @GetMapping("/page")
    public R<Page<Employee>> page(int page,int pageSize,String name){//name参数用于接收前端的搜索查询
        //1.构造分页构造器
        Page<Employee> pageInfo = new Page<>(page, pageSize);
        //2.构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //此处like方法有三个参数：第一个为布尔类型 只有为true时才将该构造条件添加到sql中执行；第二个数据库字段名；第三个字段值
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);//添加过滤条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //3.执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }
    /**
     * 根据id修改(添加)员工信息
     * */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        //已弃用，使用公共字段自动填充
//        Long empId = (Long)request.getSession().getAttribute("employee");
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empId);
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }
    /**
     *  根据id查询员工信息
     * */
    @GetMapping("/{id}") //前端通过路径传参后端接收方式
    public R<Employee> getById(@PathVariable String id){
        Employee employee = employeeService.getById(id);
        if(employee!=null){
            return R.success(employee);
        }else {
            return R.error("没有查询到对应员工信息");
        }

    }

}
