package dev.newsystem.service.impl;

import dev.newsystem.service.ImportScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImportSchedulerImpl implements ImportScheduler {

    private final FullImportServiceImpl importService;

    @Override
//    @Scheduled(cron = "0 15 1/2 * * *")
    public void scheduleImport() {
        log.info("Запуск планировщика импорта заметок");
        importService.performFullImport();
    }

}

