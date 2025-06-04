package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.TransferInfoRequestDTO;
import com.amlakie.usermanagment.dto.TransferInfoResponseDTO;

import java.util.List;

public interface TransferInfoService {
    TransferInfoResponseDTO createTransfer(TransferInfoRequestDTO transferInfoDTO);
    List<TransferInfoResponseDTO> getAllTransfers();
}