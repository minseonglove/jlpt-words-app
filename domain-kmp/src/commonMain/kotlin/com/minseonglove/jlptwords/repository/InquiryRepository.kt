package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.entity.InquiryInfo

interface InquiryRepository {
    suspend fun getInquiryInfo(): InquiryInfo
}
