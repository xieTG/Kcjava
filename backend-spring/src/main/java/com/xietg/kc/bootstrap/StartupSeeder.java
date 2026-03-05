package com.xietg.kc.bootstrap;

import com.xietg.kc.config.AppProperties;
import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.entity.UserRole;
import com.xietg.kc.db.repo.AnswerRepository;
import com.xietg.kc.db.repo.QuestionRepository;
import com.xietg.kc.db.repo.QuestionnaireRepository;
import com.xietg.kc.db.repo.SubmissionRepository;
import com.xietg.kc.db.repo.UserRepository;
import com.xietg.kc.log.Log;
import com.xietg.kc.security.PasswordService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;

@Component
public class StartupSeeder implements ApplicationRunner {

   

    private final AppProperties props;
    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final PasswordService passwordService;

    public StartupSeeder(AppProperties props, 
    		UserRepository userRepository,
    		AnswerRepository answerRepository,
    		QuestionRepository questionRepository,
    		QuestionnaireRepository questionnaireRepository,
    		SubmissionRepository submissionRepository,
    		PasswordService passwordService) {
    	
        this.props = props;
        this.userRepository = userRepository;
		this.answerRepository = answerRepository;
		this.questionnaireRepository = questionnaireRepository;
		this.questionRepository = questionRepository;
		this.submissionRepository = submissionRepository;
        this.passwordService = passwordService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // create upload directory
    	Path uploaddirpath = props.getUploadDir();
    	StartupSeeder.deleteRecursively(uploaddirpath);
    	Log.info("Create: "+uploaddirpath);
    	Files.createDirectories(uploaddirpath);
    	
    }
    

    // Delete all the content of a directory before deleting it
    private static void deleteRecursively(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);     // delete files as you go
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) throw exc; // rethrow if traversal failed
                Files.delete(dir);          // delete directory after its contents
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
