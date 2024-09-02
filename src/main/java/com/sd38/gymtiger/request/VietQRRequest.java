package com.sd38.gymtiger.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VietQRRequest {
    private String accountNo;
    private String accountName;
    private String acqId;
    private Long amount;
    private String addInfo;
    private String format;
    private String template;
}
