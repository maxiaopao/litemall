var util = require('../../../utils/util.js');
var api = require('../../../config/api.js');
var app = getApp();

Page({
  data: {
    addressList: [],
  },
  onLoad: function(options) {
    // 页面初始化 options为页面跳转所带来的参数
  },
  onReady: function() {
    // 页面渲染完成
  },
  onShow: function() {
    // 页面显示
    this.getAddressList();
  },
  getAddressList() {
    let that = this;
    util.request(api.AddressList).then(function(res) {
      if (res.errno === 0) {
        that.setData({
          addressList: res.data
        });
      } else {
        wx.showToast({
          image: '/static/images/icon_error.png',
          title: res.errmsg
        })
      }
    });
  },
  setDefaultAddress: function(e) {
    var t = this,
      d = e.currentTarget.dataset.index,
      address = t.data.addressList[d];
    
    util.request(api.AddressSetDefault, {
      id: address.id,
      version: address.version
    }, 'POST').then(function (res) {
      console.log(res);
      if (res.errno === 0) {

        var a = t.data.addressList;
        for (var e in a){
          a[e].isDefault = e == d ? 1 : 0;
        }
          
        t.setData({
          addressList: a
        });

      }
    });

  },
  addressAddOrUpdate(event) {
    console.log(event.currentTarget.dataset.addressId)

    //返回之前，先取出上一页对象，并设置addressId
    var pages = getCurrentPages();
    var prevPage = pages[pages.length - 2];

    if (prevPage.route == "pages/checkout/checkout") {
      try {
        wx.setStorageSync('addressId', event.currentTarget.dataset.addressId);
      } catch (e) {

      }

      wx.navigateBack();
    } else {
      wx.navigateTo({
        url: '/pages/ucenter/addressAdd/addressAdd?id=' + event.currentTarget.dataset.addressId
      })
    }
  },
  deleteAddress(event) {
    console.log(event.currentTarget.dataset.addressId)
    let that = this;
    wx.showModal({
      title: '',
      content: '确定要删除地址？',
      success: function(res) {
        if (res.confirm) {
          let addressId = event.currentTarget.dataset.addressId;
          util.request(api.AddressDelete, {
            id: addressId
          }, 'POST').then(function(res) {
            if (res.errno === 0) {
              that.getAddressList();
              wx.removeStorage({
                key: 'addressId',
                success: function(res) {},
              })
            }
          });
          console.log('用户点击确定')
        }
      }
    })
    return false;

  },
  onHide: function() {
    // 页面隐藏
  },
  onUnload: function() {
    // 页面关闭
  }
})