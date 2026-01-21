package br.com.mikrotik.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResultDTO {
    @Builder.Default
    private Integer totalMikrotikUsers = 0;

    @Builder.Default
    private Integer syncedUsers = 0;

    @Builder.Default
    private Integer skippedUsers = 0;

    @Builder.Default
    private Integer failedUsers = 0;

    @Builder.Default
    private List<String> syncedUsernames = new ArrayList<>();

    @Builder.Default
    private List<String> skippedUsernames = new ArrayList<>();

    @Builder.Default
    private List<String> errorMessages = new ArrayList<>();
}
