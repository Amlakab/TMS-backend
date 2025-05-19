package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.TransferInfoRequestDTO;
import com.amlakie.usermanagment.dto.TransferInfoResponseDTO;

public interface TransferInfoService {
    TransferInfoResponseDTO createTransfer(TransferInfoRequestDTO transferInfoDTO);
}