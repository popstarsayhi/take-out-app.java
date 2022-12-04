package com.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.entity.Employee;
import com.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    /**
     * employee login
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1. encryption of the password submitted (md5)
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2. search username in db
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper); //in db, username is unique, therefore using getOne method

        //3. if there is no such username, then return failure
        if(emp == null){
            return R.error("login failed, there is no such username");
        }

        //4. compare the password, if not, then return failure
        if(!emp.getPassword().equals(password)){
            return R.error("login failed, password is not matching");
        }

        //5. check the status of employee to see forbidden or normal
        if(emp.getStatus() == 0){
            return R.error("login failed, account is not active anymore");
        }

        //6. login successfully, and save id in Session and return success
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /**
     * employee logout
     * @return
     */
    @PostMapping ("/logout")
    public R<String> logout(HttpServletRequest request){
        //1. clean all employee data in Session
        request.getSession().removeAttribute("employee");
        return R.success("logout successfully");
    }

    /**
     * add new employee
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("new employee: {}",employee.toString() );

        //set password and use md5
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //add time
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        //get user id
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empId);

        //employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("new employee is added now");
    }

    /**
     * search employee info in different pages
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        //pagination constructor
        Page pageInfo = new Page(page, pageSize);

        //filtering name (where) constructor
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();

        //adding filter (where name like ...)
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);

        //adding sorting
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //excute
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * based on id, update employee info
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());

        long id = Thread.currentThread().getId();
        log.info("thread id is {}", id);

        //Long empId= (Long) request.getSession().getAttribute("employee");

       // employee.setUpdateTime(LocalDateTime.now());
       // employee.setUpdateUser(empId);

        employeeService.updateById(employee);
        return R.success("updated successfully");
    }

    /**
     * search employee bsed on id
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("search employee based on id");

        Employee employee = employeeService.getById(id);
        if(employee !=null){
            return R.success(employee);
        }
        return R.error("there is no such employee");
    }

}
