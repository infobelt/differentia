package com.infobelt.differentia;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(exclude = "bun")
@EqualsAndHashCode(of = "id")
@AuditMetadata(parent = "bun", mappedBy = "hotDogs")
public class HotDog {

    private Long id;

    private Bun bun;

}
