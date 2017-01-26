package uk.ac.ebi.subs.upload;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.data.files.FileRecord;
import uk.ac.ebi.subs.data.status.SubmissionStatus;
import uk.ac.ebi.subs.repository.FileRecordRepository;
import uk.ac.ebi.subs.repository.SubmissionRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@Controller
/**
 * Accept file uploads within a submission
 * Writes files to a base upload path, one directory per submission
 *
 */
public class FileUploadController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private FileRecordRepository fileRecordRepository;

    @Value("${base-upload-path:.}")
    private String baseUploadPath;


    @RequestMapping(value = "/submissions/{submissionId}/upload", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity<String> upload(@PathVariable String submissionId, HttpServletRequest request) {

        Submission submission = submissionRepository.findOne(submissionId);
        if (submission == null) {
            throw new ResourceNotFoundException();
        }
        if (!SubmissionStatus.Draft.name().equals(submission.getStatus())) {
            return ResponseEntity.badRequest().body("Submission is locked"); //TODO improve error messages
        }
        //TODO add check for ownership once we have AAP in place


        try {
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);

            if (!isMultipart) {
                // Inform user about invalid request
                return ResponseEntity.badRequest().body("Must be a multi-part request"); //TODO improve error messages
            }

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload();

            // Parse the request
            FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();

                if (!item.isFormField()) {
                    String filename = item.getName();
                    // Process the input stream

                    Path targetDir = Paths.get(baseUploadPath, submissionId);

                    targetDir.toFile().mkdirs();

                    Path targetPath = Paths.get(baseUploadPath, submissionId, filename);

                    logger.info("copying uploaded stream to {}", targetPath);

                    Files.copy(stream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    stream.close();

                    FileRecord fileRecord = fileRecordRepository.findBySubmissionIdAndFileName(submissionId, filename);

                    if (fileRecord == null) {
                        fileRecord = new FileRecord();
                        fileRecord.setSubmission(submission);
                        fileRecord.setFileName(filename);
                    }

                    fileRecord.setContentType(item.getContentType());
                    fileRecord.setSizeInBytes(targetPath.toFile().length());

                    fileRecordRepository.save(fileRecord);


                }
            }
        } catch (FileUploadException e) {
            logger.error("FileUpload error: {}", e);
            return ResponseEntity.badRequest().body("File upload error:" + e.toString()); //TODO this should be a 500 error
        } catch (IOException e) {
            logger.error("IO error: {}", e);
            return ResponseEntity.badRequest().body("Internal server IO error"); //TODO this should be a 500 error
        }

        return ResponseEntity.ok().body("Success");
    }
}
