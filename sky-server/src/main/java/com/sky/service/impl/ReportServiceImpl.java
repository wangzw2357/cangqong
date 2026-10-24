package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 统计指定时间区间内的营业额数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //查询date日期对应的营业额，营业额是指：状态为“已完成”的订单金额合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("status", Orders.COMPLETED);
            map.put("begin", beginTime);
            map.put("end", endTime);
            Double turnover = ordersMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.collectionToCommaDelimitedString(dateList))
                .turnoverList(StringUtils.collectionToCommaDelimitedString(turnoverList))
                .build();
    }

    /**
     * 统计指定时间区间内的用户数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //存放从begin到end之间的每天日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天的新增用户数量
        List<Integer> newUserList = new ArrayList<>();
        //存放每天的用户总量
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //用户总量：截止date当天24时的用户数量
            Map map = new HashMap();
            map.put("end", endTime);
            totalUserList.add(userMapper.countByMap(map));

            //新增用户：date当天注册的用户数量
            map.put("begin", beginTime);
            newUserList.add(userMapper.countByMap(map));
        }

        return UserReportVO
                .builder()
                .dateList(StringUtils.collectionToCommaDelimitedString(dateList))
                .totalUserList(StringUtils.collectionToCommaDelimitedString(totalUserList))
                .newUserList(StringUtils.collectionToCommaDelimitedString(newUserList))
                .build();
    }

    /**
     * 统计指定时间区间内的订单数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天的订单总数
        List<Integer> orderCountList = new ArrayList<>();
        //存放每天的有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //每天的订单总数
            orderCountList.add(getOrderCount(beginTime, endTime, null));
            //每天的有效订单数（已完成状态）
            validOrderCountList.add(getOrderCount(beginTime, endTime, Orders.COMPLETED));
        }

        //时间区间内的订单总数
        Integer totalOrderCount = getOrderCount(LocalDateTime.of(dateList.get(0), LocalTime.MIN),
                LocalDateTime.of(dateList.get(dateList.size() - 1), LocalTime.MAX), null);
        //时间区间内的有效订单数
        Integer validOrderCount = getOrderCount(LocalDateTime.of(dateList.get(0), LocalTime.MIN),
                LocalDateTime.of(dateList.get(dateList.size() - 1), LocalTime.MAX), Orders.COMPLETED);

        //订单完成率 = 有效订单数 / 订单总数
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO
                .builder()
                .dateList(StringUtils.collectionToCommaDelimitedString(dateList))
                .orderCountList(StringUtils.collectionToCommaDelimitedString(orderCountList))
                .validOrderCountList(StringUtils.collectionToCommaDelimitedString(validOrderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 根据时间区间和订单状态统计订单数量
     *
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("status", status);
        map.put("begin", begin);
        map.put("end", end);
        return ordersMapper.countByMap(map);
    }

    /**
     * 统计指定时间区间内的销量排名前10商品
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = ordersMapper.getSalesTop10(beginTime, endTime);

        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.collectionToCommaDelimitedString(names))
                .numberList(StringUtils.collectionToCommaDelimitedString(numbers))
                .build();
    }

    /**
     * 导出最近30天的运营数据报表并下载
     *
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据——查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        //2. 通过POI将数据写入到Excel文件中
        XSSFWorkbook excel = new XSSFWorkbook();
        XSSFSheet sheet = excel.createSheet("运营数据概览");
        XSSFRow row = sheet.createRow(0);
        row.createCell(0).setCellValue("日期");
        row.createCell(1).setCellValue("新增用户数");
        row.createCell(2).setCellValue("订单数");
        row.createCell(3).setCellValue("有效订单数");
        row.createCell(4).setCellValue("营业额");

        int rowIndex = 1;
        for (LocalDate date = dateBegin; !date.isAfter(dateEnd); date = date.plusDays(1)) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map userMap = new HashMap();
            userMap.put("begin", beginTime);
            userMap.put("end", endTime);
            Integer newUserCount = userMapper.countByMap(userMap);

            Integer orderCount = getOrderCount(beginTime, endTime, null);
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            Map turnoverMap = new HashMap();
            turnoverMap.put("status", Orders.COMPLETED);
            turnoverMap.put("begin", beginTime);
            turnoverMap.put("end", endTime);
            Double turnover = ordersMapper.sumByMap(turnoverMap);
            turnover = turnover == null ? 0.0 : turnover;

            row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(date.toString());
            row.createCell(1).setCellValue(newUserCount);
            row.createCell(2).setCellValue(orderCount);
            row.createCell(3).setCellValue(validOrderCount);
            row.createCell(4).setCellValue(turnover);
        }

        //第二个sheet：订单明细
        XSSFSheet detailSheet = excel.createSheet("订单明细");
        XSSFRow detailRow = detailSheet.createRow(0);
        detailRow.createCell(0).setCellValue("订单号");
        detailRow.createCell(1).setCellValue("菜品名称");
        detailRow.createCell(2).setCellValue("菜品单价");
        detailRow.createCell(3).setCellValue("份数");
        detailRow.createCell(4).setCellValue("下单时间");

        List<Map<String, Object>> detailList = orderDetailMapper.listDetailByOrderTime(
                LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));
        int detailRowIndex = 1;
        for (Map<String, Object> detail : detailList) {
            detailRow = detailSheet.createRow(detailRowIndex++);
            detailRow.createCell(0).setCellValue(String.valueOf(detail.get("orderNumber")));
            detailRow.createCell(1).setCellValue(String.valueOf(detail.get("name")));
            Object amount = detail.get("amount");
            detailRow.createCell(2).setCellValue(amount == null ? 0 : ((BigDecimal) amount).doubleValue());
            Object number = detail.get("number");
            detailRow.createCell(3).setCellValue(number == null ? 0 : ((Number) number).intValue());
            detailRow.createCell(4).setCellValue(String.valueOf(detail.get("orderTime")));
        }

        //3. 通过输出流将Excel文件下载到客户端浏览器
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("运营数据报表", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
