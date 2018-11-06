package org.linlinjava.litemall.wx.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.core.util.RegexUtil;
import org.linlinjava.litemall.db.domain.LitemallAddress;
import org.linlinjava.litemall.db.domain.LitemallRegion;
import org.linlinjava.litemall.db.service.LitemallAddressService;
import org.linlinjava.litemall.db.service.LitemallRegionService;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.wx.annotation.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wx/address")
@Validated
public class WxAddressController {
    private final Log logger = LogFactory.getLog(WxAddressController.class);

    @Autowired
    private LitemallAddressService addressService;
    @Autowired
    private LitemallRegionService regionService;

    /**
     * 用户收货地址列表
     *
     * @param userId 用户ID
     * @return 收货地址列表
     *   成功则
     *  {
     *      errno: 0,
     *      errmsg: '成功',
     *      data: xxx
     *  }
     *   失败则 { errno: XXX, errmsg: XXX }
     */
    @GetMapping("list")
    public Object list(@LoginUser Integer userId) {
        if(userId == null){
            return ResponseUtil.unlogin();
        }
        List<LitemallAddress> addressList = addressService.queryByUid(userId);
        List<Map<String, Object>> addressVoList = new ArrayList<>(addressList.size());
        for(LitemallAddress address : addressList){
            Map<String, Object> addressVo = new HashMap<>();
            addressVo.put("id", address.getId());
            addressVo.put("name", address.getName());
            addressVo.put("mobile", address.getMobile());
            addressVo.put("isDefault", address.getIsDefault());
            addressVo.put("version", address.getVersion());
            String province = regionService.findById(address.getProvinceId()).getName();
            String city = regionService.findById(address.getCityId()).getName();
            String area = regionService.findById(address.getAreaId()).getName();
            String addr = address.getAddress();
            String detailedAddress = province + city + area + " " + addr;
            addressVo.put("detailedAddress", detailedAddress);

            addressVoList.add(addressVo);
        }
        return ResponseUtil.ok(addressVoList);
    }
    
    @GetMapping("district")
    public Object district(@LoginUser Integer userId,String provinceName,String cityName,String districtName) {
        if(userId == null){
            return ResponseUtil.unlogin();
        }
        
        LitemallRegion  province = this.regionService.findByName(provinceName, new Byte("1"));
        LitemallRegion  city = this.regionService.findByName(cityName, new Byte("2"));
        LitemallRegion  district = this.regionService.findByName(districtName, new Byte("3"));
        
        Map<Object, Object> provinceMap = new HashMap<Object, Object>();
        provinceMap.put("id", province.getId());
        provinceMap.put("name", province.getName());
        
        Map<Object, Object> cityMap = new HashMap<Object, Object>();
        cityMap.put("id", city.getId());
        cityMap.put("name", city.getName());
        
        Map<Object, Object> districtMap = new HashMap<Object, Object>();
        districtMap.put("id", district.getId());
        districtMap.put("name", district.getName());
        
        Map<Object, Object> dataMap = new HashMap<Object, Object>();
        dataMap.put("province", provinceMap);
        dataMap.put("city", cityMap);
        dataMap.put("district", districtMap);
        
        return ResponseUtil.ok(dataMap);
    }

    /**
     * 收货地址详情
     *
     * @param userId 用户ID
     * @param id 收获地址ID
     * @return 收货地址详情
     *   成功则
     *  {
     *      errno: 0,
     *      errmsg: '成功',
     *      data:
     *        {
     *           id: xxx,
     *           name: xxx,
     *           provinceId: xxx,
     *           cityId: xxx,
     *           districtId: xxx,
     *           mobile: xxx,
     *           address: xxx,
     *           isDefault: xxx,
     *           provinceName: xxx,
     *           cityName: xxx,
     *           areaName: xxx
     *        }
     *  }
     *   失败则 { errno: XXX, errmsg: XXX }
     */
    @GetMapping("detail")
    public Object detail(@LoginUser Integer userId, @NotNull Integer id) {
        if(userId == null){
            return ResponseUtil.unlogin();
        }

        LitemallAddress address = addressService.findById(id);
        if(address == null){
            return ResponseUtil.badArgumentValue();
        }

        Map<Object, Object> data = new HashMap<Object, Object>();
        data.put("id", address.getId());
        data.put("name", address.getName());
        data.put("provinceId", address.getProvinceId());
        data.put("cityId", address.getCityId());
        data.put("districtId", address.getAreaId());
        data.put("mobile", address.getMobile());
        data.put("address", address.getAddress());
        data.put("isDefault", address.getIsDefault());
        String pname = regionService.findById(address.getProvinceId()).getName();
        data.put("provinceName", pname);
        String cname = regionService.findById(address.getCityId()).getName();
        data.put("cityName", cname);
        String dname = regionService.findById(address.getAreaId()).getName();
        data.put("areaName", dname);
        data.put("version", address.getVersion());
        return ResponseUtil.ok(data);
    }

    private Object validate(LitemallAddress address) {
        String name = address.getName();
        if(StringUtils.isEmpty(name)){
            return ResponseUtil.badArgument();
        }

        // 测试收货手机号码是否正确
        String mobile = address.getMobile();
        if(StringUtils.isEmpty(mobile)){
            return ResponseUtil.badArgument();
        }
        if(!RegexUtil.isMobileExact(mobile)){
            return ResponseUtil.badArgument();
        }

        Integer pid = address.getProvinceId();
        if(pid == null){
            return ResponseUtil.badArgument();
        }
        if(addressService.findById(pid) == null){
            return ResponseUtil.badArgumentValue();
        }

        Integer cid = address.getCityId();
        if(cid == null){
            return ResponseUtil.badArgument();
        }
        if(addressService.findById(cid) == null){
            return ResponseUtil.badArgumentValue();
        }

        Integer aid = address.getAreaId();
        if(aid == null){
            return ResponseUtil.badArgument();
        }
        if(addressService.findById(aid) == null){
            return ResponseUtil.badArgumentValue();
        }

        String detailedAddress = address.getAddress();
        if(StringUtils.isEmpty(detailedAddress)){
            return ResponseUtil.badArgument();
        }

        Boolean isDefault = address.getIsDefault();
        if(isDefault == null){
            return ResponseUtil.badArgument();
        }
        return null;
    }

    /**
     * 添加或更新收货地址
     *
     * @param userId 用户ID
     * @param address 用户收货地址
     * @return 添加或更新操作结果
     *   成功则 { errno: 0, errmsg: '成功' }
     *   失败则 { errno: XXX, errmsg: XXX }
     */
    @PostMapping("save")
    public Object save(@LoginUser Integer userId, @RequestBody LitemallAddress address) {
        if(userId == null){
            return ResponseUtil.unlogin();
        }
//        Object error = validate(address);
//        if(error != null){
//            return error;
//        }

        if(address.getIsDefault()){
            // 重置其他收获地址的默认选项
            addressService.resetDefault(userId);
        }

        if (address.getId() == null || address.getId().equals(0)) {
            address.setId(null);
            address.setAddTime(LocalDateTime.now());
            address.setUserId(userId);
            addressService.add(address);
        } else {
            address.setUserId(userId);
            if(addressService.updateId(address) == 0){
                return ResponseUtil.updatedDateExpired();
            }
        }
        return ResponseUtil.ok(address.getId());
    }
    
    @PostMapping("resetDefault")
    public Object resetDefault(@LoginUser Integer userId, @RequestBody LitemallAddress address) {
        if(userId == null){
            return ResponseUtil.unlogin();
        }

        // 重置其他收获地址的默认选项
        addressService.resetDefault(userId);
        address.setIsDefault(true);
        addressService.updateId(address);
        
        return ResponseUtil.ok(address);
    }

    /**
     * 删除收货地址
     *
     * @param userId 用户ID
     * @param address 用户收货地址
     * @return 删除结果
     *   成功则 { errno: 0, errmsg: '成功' }
     *   失败则 { errno: XXX, errmsg: XXX }
     */
    @PostMapping("delete")
    public Object delete(@LoginUser Integer userId, @RequestBody LitemallAddress address) {
        if(userId == null){
            return ResponseUtil.unlogin();
        }
        Integer id = address.getId();
        if(id == null){
            return ResponseUtil.badArgument();
        }

        addressService.delete(id);
        return ResponseUtil.ok();
    }
}