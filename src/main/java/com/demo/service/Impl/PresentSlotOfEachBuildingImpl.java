package com.demo.service.Impl;

import com.demo.entity.Booking;
import com.demo.entity.Customer_Slot;
import com.demo.entity.Resident_Slot;
import com.demo.repository.AreaRepository;
import com.demo.repository.BookingRepository;
import com.demo.repository.Customer_Slot_Repository;
import com.demo.repository.Resident_Slot_Repository;
import com.demo.service.PresentSlotOfEachBuilding;
import com.demo.utils.request.DateDTO;
import com.demo.utils.response.PresentSlotResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

@Service
@Slf4j
public class PresentSlotOfEachBuildingImpl implements PresentSlotOfEachBuilding {
    @Autowired
    Customer_Slot_Repository customer_slot_repository;

    @Autowired
    Resident_Slot_Repository resident_slot_repository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    AreaRepository areaRepository;

    @Override
    public List<PresentSlotResponseDto> findAll(String id_Building, DateDTO dto) {
        List<PresentSlotResponseDto> list = new ArrayList<>();
        List<Customer_Slot> listCustomerSlot = customer_slot_repository.findAllSlotOfEachBuilding(id_Building);
        List<Resident_Slot> listResidentSlot = resident_slot_repository.findAllSlotOfEachBuilding(id_Building);
        for (int i = 0; i < listCustomerSlot.size(); i++) {
            Customer_Slot customerSlot = listCustomerSlot.get(i);
            if (checkSlotisEmpty(dto, customerSlot))
            {
                customerSlot.setStatus_Slots(true);
            }
            else customerSlot.setStatus_Slots(false);
            list.add(new PresentSlotResponseDto(customerSlot.getId_C_Slot(), id_Building, customerSlot.isStatus_Slots()));
        }
        for (int i = 0; i < listResidentSlot.size(); i++) {
            Resident_Slot residentSlot = listResidentSlot.get(i);
            list.add(new PresentSlotResponseDto(residentSlot.getId_R_Slot(), id_Building, residentSlot.isStatus_Slots()));
        }
        return list;
    }

    private boolean checkSlotisEmpty(DateDTO dto, Customer_Slot customerSlot) {
        List<Booking> bookingList = bookingRepository.findAll();
        for (Booking booking : bookingList) {
            Customer_Slot slot = booking.getCustomer_slot();
            if (booking.is_deleted() == false && booking.is_enabled() == true && slot.getId_C_Slot() == customerSlot.getId_C_Slot()) {

                TimeZone vietnamTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
                Calendar calendar = Calendar.getInstance(vietnamTimeZone);

                //xet thong tin tu FE xet dto
                calendar.setTime(dto.getStartDate());
                int dto_month_start = calendar.get(Calendar.MONTH) + 1; // Note: Calendar.MONTH is zero-based, so add 1
                calendar.setTime(dto.getEndDate());
                int dto_month_end = calendar.get(Calendar.MONTH) + 1; // Note: Calendar.MONTH is zero-based, so add 1
                calendar.setTime(dto.getStartDate());
                int dto_day_start = calendar.get(Calendar.DAY_OF_MONTH);
                calendar.setTime(dto.getEndDate());
                int dto_day_end = calendar.get(Calendar.DAY_OF_MONTH);
                int dto_hh_st = Integer.parseInt(dto.getStartTime().substring(0, dto.getStartTime().indexOf(':')));
                int dto_hh_en = Integer.parseInt(dto.getEndTime().substring(0, dto.getEndTime().indexOf(':')));

                //Day la Real time tu FE gui xuong
                int dto_hours = Integer.parseInt(dto.getTime().substring(0, dto.getTime().indexOf('a')));

           //     log.info(dto_month_start + " " + dto_month_end +  " " + dto_day_start + " " + dto_day_end + " " + dto_hh_st + " " + dto_hh_en);


                //Day la thoi gian va Date lay trong DB ra
                calendar.setTime(booking.getStartDate());
                int bk_month_start = calendar.get(Calendar.MONTH) + 1; // Note: Calendar.MONTH is zero-based, so add 1
                calendar.setTime(booking.getEndDate());
                int bk_month_end = calendar.get(Calendar.MONTH) + 1; // Note: Calendar.MONTH is zero-based, so add 1
                calendar.setTime(booking.getStartDate());
                int bk_day_start = calendar.get(Calendar.DAY_OF_MONTH);
                calendar.setTime(booking.getEndDate());
                int bk_day_end = calendar.get(Calendar.DAY_OF_MONTH);
                int bk_hh_st = Integer.parseInt(booking.getStartTime().substring(0, dto.getStartTime().indexOf(':')));
                int bk_hh_en = Integer.parseInt(booking.getEndTime().substring(0, dto.getEndTime().indexOf(':')));

//                log.info(bk_month_start + " " + bk_month_end +  " " + bk_day_start + " " + bk_day_end + " " + bk_hh_st + " " + bk_hh_en);
                // TH1
                if (
                        (
                                (bk_month_end > dto_month_end) ||
                                        (bk_month_end == dto_month_end && bk_day_end > dto_day_end) ||
                                        (bk_month_end == dto_month_end && bk_day_end == dto_day_end && bk_hh_en >= dto_hh_en)
                        )
                                &&
                                (
                                        (bk_month_start < dto_month_end) ||
                                                (bk_month_start == dto_month_end && bk_day_start < dto_day_end) ||
                                                (bk_month_start == dto_month_end && bk_day_start == dto_day_end && bk_hh_st <= dto_hh_en)
                                )
                ) {
                    return true;
                }

                //TH2
                if (
                        (
                                (bk_month_end > dto_month_start) ||
                                        (bk_month_end == dto_month_start && bk_day_end > dto_day_start) ||
                                        (bk_month_end == dto_month_start && bk_day_end == dto_day_start && bk_hh_en >= dto_hh_st)
                        )
                                &&
                                (
                                        (bk_month_start < dto_month_start) ||
                                                (bk_month_start == dto_month_start && bk_day_start < dto_day_start) ||
                                                (bk_month_start == dto_month_start && bk_day_start == dto_day_start && bk_hh_st <= dto_hh_st)
                                )
                ) {
                    return true;
                }

                //TH3
                if (
                        (
                                (bk_month_end > dto_month_end) ||
                                        (bk_month_end == dto_month_end && bk_day_end > dto_day_end) ||
                                        (bk_month_end == dto_month_end && bk_day_end == dto_day_end && bk_hh_en >= dto_hh_en)
                        )
                                &&
                                (
                                        (bk_month_start < dto_month_start) ||
                                                (bk_month_start == dto_month_start && bk_day_start < dto_day_start) ||
                                                (bk_month_start == dto_month_start && bk_day_start == dto_day_start && bk_hh_st <= dto_hh_st)
                                )
                ) {
                    return true;
                }

                //TH4
                if (
                        (
                                (bk_month_end < dto_month_end) ||
                                        (bk_month_end == dto_month_end && bk_day_end < dto_day_end) ||
                                        (bk_month_end == dto_month_end && bk_day_end == dto_day_end && bk_hh_en <= dto_hh_en)
                        )
                                &&
                                (
                                        (bk_month_start > dto_month_start) ||
                                                (bk_month_start == dto_month_start && bk_day_start > dto_day_start) ||
                                                (bk_month_start == dto_month_start && bk_day_start == dto_day_start && bk_hh_st >= dto_hh_st)
                                )
                ) {
                    return true;
                }
            }
        }
        return false;
    }
}
