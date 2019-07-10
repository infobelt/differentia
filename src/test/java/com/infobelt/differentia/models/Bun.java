package com.infobelt.differentia.models;

import com.infobelt.differentia.AuditMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@AuditMetadata(name = "Bun")
@Data
@EqualsAndHashCode(of = "id")
public class Bun {

    private Long id;

    private List<HotDog> hotDogs = new ArrayList<>();

}
