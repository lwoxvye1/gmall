package com.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.gmall.annotations.LoginRequired;
import com.gmall.bean.OmsCartItem;
import com.gmall.bean.PmsSkuInfo;
import com.gmall.service.CartService;
import com.gmall.service.SkuService;
import com.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@CrossOrigin
public class CartController {
    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;

    @LoginRequired(loginSuccess = false)
    @RequestMapping("/addToCart")
    public String addToCart(String skuId, BigDecimal quantity,
                            HttpServletRequest request, HttpServletResponse response){
        String memberId = (String)request.getAttribute("memberId");
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);
        List<OmsCartItem> omsCartItems = new ArrayList<>();

        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("111111111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(quantity);

        if (StringUtils.isBlank(memberId)){
            // 用户没有登录，使用Cookie
            String cartListCookie = CookieUtil.getCookieValue(
                    request, "cartListCookie", true);

            if (StringUtils.isBlank(cartListCookie)) {
                omsCartItems.add(omsCartItem);
            } else {
                omsCartItems = JSON.parseArray(cartListCookie,
                        OmsCartItem.class);
                boolean exist = if_cart_exist(omsCartItems, omsCartItem);
                if (exist) {
                    for (OmsCartItem cartItem : omsCartItems) {
                        if (cartItem.getProductSkuId().equals(
                                omsCartItem.getProductSkuId())){
                            cartItem.setQuantity(cartItem.getQuantity().add(
                                    omsCartItem.getQuantity()));
                            break;
                        }
                    }
                } else {
                    omsCartItems.add(omsCartItem);
                }
            }

            CookieUtil.setCookie(request, response, "cartListCookie",
                    JSON.toJSONString(omsCartItems), 60 * 60 * 72,
                    true);
        } else {
            // 用户已经操作，使用DB+Cache
            OmsCartItem omsCartItemFromDb = cartService.ifCartExistByUser(memberId, skuId);

            if (omsCartItemFromDb == null){
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname("test小明");
                cartService.addCart(omsCartItem);
            } else {
                omsCartItemFromDb.setQuantity(omsCartItem.getQuantity().add(
                        omsCartItemFromDb.getQuantity()));
                cartService.updateCart(omsCartItemFromDb);
            }

            // 同步缓存
            cartService.syncCartCache(memberId);
        }
        return "redirect:/success.html";
    }

    @LoginRequired(loginSuccess = false)
    @RequestMapping("/cartList")
    public String cartList(HttpServletRequest request, ModelMap modelMap){
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        String memberId = (String)request.getAttribute("memberId");

        if (StringUtils.isNotBlank(memberId)) {
            omsCartItems = cartService.cartList(memberId);
        } else {
            String cartListCookie = CookieUtil.getCookieValue(request,
                    "cartListCookie", true);
            if (cartListCookie != null && cartListCookie.length() != 0){
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
            }
        }

        for (OmsCartItem omsCartItem : omsCartItems) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        }
        modelMap.put("cartList", omsCartItems);

        // 被勾选商品的总额
        BigDecimal totalAmount = getTotalAmount(omsCartItems);
        modelMap.put("totalAmount", totalAmount);
        return "cartList";
    }

    @LoginRequired(loginSuccess = false)
    @RequestMapping("/checkCart")
    public String checkCart(String isChecked, String skuId, ModelMap modelMap,
                            HttpServletRequest request){
        String memberId = (String)request.getAttribute("memberId");

        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setIsChecked(isChecked);
        cartService.checkCart(omsCartItem);
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        modelMap.put("cartList", omsCartItems);

        BigDecimal totalAmount = getTotalAmount(omsCartItems);
        modelMap.put("totalAmount", totalAmount);
        return "cartListInner";
    }

    private boolean if_cart_exist(List<OmsCartItem> omsCartItems,
                                  OmsCartItem omsCartItem) {
        boolean flag = false;
        for (OmsCartItem cartItem : omsCartItems) {
            String productSkuId = cartItem.getProductSkuId();
            if (productSkuId.equals(omsCartItem.getProductSkuId())){
                flag = true;
                break;
            }
        }
        return flag;
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) {
            if ("1".equals(omsCartItem.getIsChecked())) {
                BigDecimal totalPrice = omsCartItem.getTotalPrice();
                totalAmount = totalAmount.add(totalPrice);
            }
        }
        return totalAmount;
    }
}
