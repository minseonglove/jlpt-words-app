package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.InquiryInfo
import com.minseonglove.jlptwords.repository.InquiryRepository

class GetInquiryInfo(
    private val inquiryRepository: InquiryRepository,
) {
    suspend operator fun invoke(): InquiryInfo {
        return inquiryRepository.getInquiryInfo()
    }
}
