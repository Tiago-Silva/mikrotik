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
    private Integer totalMikrotikUsers;
    private Integer syncedUsers;
    private Integer skippedUsers;
    private Integer failedUsers;
    private List<String> syncedUsernames;
    private List<String> skippedUsernames;
    private List<String> errorMessages;

    public SyncResultDTO() {
        this.syncedUsernames = new ArrayList<>();
        this.skippedUsernames = new ArrayList<>();
        this.errorMessages = new ArrayList<>();
        this.totalMikrotikUsers = 0;
        this.syncedUsers = 0;
        this.skippedUsers = 0;
        this.failedUsers = 0;
    }
}
