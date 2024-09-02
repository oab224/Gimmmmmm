
package com.sd38.gymtiger.request;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GHNRequest {

    private Integer service_type_id;

    private Integer from_district_id;

    private String from_ward_code;

    private Integer to_district_id;

    private String to_ward_code;

    private Integer weight;


}
