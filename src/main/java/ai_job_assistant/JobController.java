package ai_job_assistant;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.jsoup.Jsoup;

@RestController
@RequestMapping("/api")
public class JobController {

    @PostMapping("/analyze")
    public String analyze(@RequestParam("cv") MultipartFile cv,
                          @RequestParam("jobUrl") String jobUrl) {
        try {
            //CV
            byte[] pdfBytes = cv.getBytes();

            PDDocument document = Loader.loadPDF(pdfBytes);

            PDFTextStripper stripper = new PDFTextStripper();
            String cvText = stripper.getText(document);

            document.close();

            System.out.println("CV: " + cv.getOriginalFilename());
            System.out.println("Job URL: " + jobUrl);
            System.out.println("----- CV TEXT -----");
            System.out.println(cvText);
            System.out.println("-------------------");

            //Jobbannonsen
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(jobUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            String jobHtml = response.body();

            var htmlDocument = Jsoup.parse(jobHtml);

            htmlDocument.select("script, style, nav, footer, header").remove();

            String jobText = htmlDocument.body().text();

            System.out.println("----- JOB AD TEXT -----");
            System.out.println(jobText);
            System.out.println("-----------------------");

            return "----- CV TEXT -----\n\n" + cvText
                    + "\n\n----- JOB AD TEXT -----\n\n" + jobText;


        } catch (Exception e) {
            e.printStackTrace();
            return "Could not analyze CV and job advertisement.";
        }
    }
}
