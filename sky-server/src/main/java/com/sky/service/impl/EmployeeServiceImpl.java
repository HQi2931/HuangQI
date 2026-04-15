package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端传来的明文密码进行MD5加密，然后再与数据库中的密文密码比对
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!md5Password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /*
     * 添加员工信息
     * */
    @Override
    public void add(EmployeeDTO employeeDTO) {
        // 将DTO转换为Entity
        Employee employee = new Employee();
        employee.setUsername(employeeDTO.getUsername());
        employee.setName(employeeDTO.getName());
        employee.setPhone(employeeDTO.getPhone());
        employee.setSex(employeeDTO.getSex());
        employee.setIdNumber(employeeDTO.getIdNumber());

        // 设置默认密码为123456，并进行MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        // 设置默认状态为启用
        employee.setStatus(StatusConstant.ENABLE);

        // 从ThreadLocal中获取当前登录员工的ID
        Long currentId = BaseContext.getCurrentId();
        employee.setCreateUser(currentId);
        employee.setUpdateUser(currentId);

        // 设置创建时间和更新时间
        employee.setCreateTime(java.time.LocalDateTime.now());
        employee.setUpdateTime(java.time.LocalDateTime.now());

        employeeMapper.add(employee);
    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //1.设置分页参数
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        //2.执行分页查询
        List<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        //查询结果并封装
        Page<Employee> p = (Page<Employee>) page;

        return new PageResult(p.getTotal(), p.getResult());
    }

    /**
     * 启用禁用员工账号
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // 更新员工状态
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .updateTime(java.time.LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        employeeMapper.update(employee);
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     */
    @Override
    public void edit(EmployeeDTO employeeDTO) {
        // 将DTO转换为Entity
        Employee employee = new Employee();
        employee.setId(employeeDTO.getId());
        employee.setUsername(employeeDTO.getUsername());
        employee.setName(employeeDTO.getName());
        employee.setPhone(employeeDTO.getPhone());
        employee.setSex(employeeDTO.getSex());
        employee.setIdNumber(employeeDTO.getIdNumber());
        
        // 设置更新时间和更新人
        employee.setUpdateTime(java.time.LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        
        employeeMapper.update(employee);
    }

    @Override
    public EmployeeDTO getById(Long id) {
        return employeeMapper.getById(id);
    }

    /**
     * 修改密码
     * @param passwordEditDTO
     */
    @Override
    public void updatePassword(PasswordEditDTO passwordEditDTO) {
        // 1. 根据当前登录用户ID查询员工信息（从BaseContext获取）
        Long empId = BaseContext.getCurrentId();
        Employee employee = employeeMapper.getByUsername(
            employeeMapper.getById(empId).getUsername()
        );
        
        // 2. 校验员工是否存在
        if (employee == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        
        // 3. 校验旧密码是否正确
        String oldPasswordMd5 = DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes());
        if (!oldPasswordMd5.equals(employee.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        
        // 4. 更新为新密码（MD5加密）
        String newPasswordMd5 = DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes());
        Employee updateEmployee = Employee.builder()
                .id(empId)
                .password(newPasswordMd5)
                .updateTime(java.time.LocalDateTime.now())
                .updateUser(empId)
                .build();
        employeeMapper.update(updateEmployee);
    }
}
