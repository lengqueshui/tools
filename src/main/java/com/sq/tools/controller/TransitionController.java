package com.sq.tools.controller;

import com.sq.tools.utils.AmountChangeUtil;
import com.sq.tools.utils.ResponseModel;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("transition")
public class TransitionController {

    private Logger logger = Logger.getLogger(getClass());

    @RequestMapping("/amount")
    @ResponseBody
    public ResponseModel changeAmt(@RequestParam(required = false) String amount) {
        if (StringUtils.isEmpty(amount)) {
            return new ResponseModel(ResponseModel.COMMON_PARAMS_INVALID);
        }

        try {
            double amountDouble = Double.parseDouble(amount);
            return new ResponseModel(AmountChangeUtil.transition(amountDouble));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseModel(ResponseModel.COMMON_PARAMS_INVALID);
        }
    }

}
