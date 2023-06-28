package com.memposit.esigning.dokobit.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FilesDto {

    @NonNull
    private UserInfo userInfo;

    @NonNull
    private List<MultipartFile> files;
}
