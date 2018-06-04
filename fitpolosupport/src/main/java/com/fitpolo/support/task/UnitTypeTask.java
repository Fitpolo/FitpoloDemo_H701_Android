package com.fitpolo.support.task;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置单位制式
 * @ClassPath com.fitpolo.support.task.UnitTypeTask
 */
public class UnitTypeTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 2;
    // 单位制式
    private static final int HEADER_SET_UNIT_TYPE = 0x23;

    private byte[] orderData;

    public UnitTypeTask(MokoOrderTaskCallback callback, int unitType) {
        super(OrderType.WRITE, OrderEnum.setUnitType, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_SET_UNIT_TYPE;
        orderData[1] = (byte) unitType;
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[1])) {
            return;
        }
        LogModule.i(order.getOrderName() + "成功");
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
