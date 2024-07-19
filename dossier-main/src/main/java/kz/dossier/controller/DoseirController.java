package kz.dossier.controller;


import com.lowagie.text.*;
import kz.dossier.modelsDossier.*;
import kz.dossier.modelsDossier.FlRelativesLevelDto;
import kz.dossier.repositoryDossier.EsfAll2Repo;
import kz.dossier.repositoryDossier.FlRelativesRepository;
import kz.dossier.repositoryDossier.MvAutoFlRepo;
import kz.dossier.repositoryDossier.NewPhotoRepo;
import kz.dossier.security.models.log;
import kz.dossier.security.repository.LogRepo;
import kz.dossier.service.FlRiskServiceImpl;
import kz.dossier.service.MyService;
import kz.dossier.tools.PdfGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3000)
@RestController
@RequestMapping("/api/pandora/dossier")
public class DoseirController {
    @Autowired
    NewPhotoRepo newPhotoRepo;
    @Autowired
    EsfAll2Repo esfAll2Repo;
    @Autowired
    MvAutoFlRepo mvAutoFlRepo;
    @Autowired
    MyService myService;
    @Autowired
    FlRelativesRepository relativesRepository;
    @Autowired
    LogRepo logRepo;
    @Autowired
    FlRiskServiceImpl flRiskService;



    @GetMapping("/profile")
    public NodesFL getProfile(@RequestParam String iin) {
        return myService.getNode(iin);
    }

    @GetMapping("/getRiskByIin")
    public FLRiskDto getRisk(@RequestParam String iin){
        return flRiskService.findFlRiskByIin(iin);
    }
    @GetMapping("/getFirstRowByIin")
    public FlFirstRowDto getFirstRow(@RequestParam String iin){
        return myService.getFlFirstRow(iin);
    }
    @GetMapping("/cc")
    public NodesUL getChfc(@RequestParam String bin) {
        NodesUL ss = myService.getNodeUL(bin);
        return ss;
    }
    @GetMapping("/taxpage")
    public List<TaxOutEntity> getTax(@RequestParam String bin, @RequestParam(required = false,defaultValue = "0") int page, @RequestParam(required = false,defaultValue = "10") int size) {
        return myService.taxOutEntities(bin,PageRequest.of(page,size));
    }

    @GetMapping("/pensionUl")
    public List<Map<String, Object>> pensionUl(@RequestParam String bin, @RequestParam String year, @RequestParam(required = false,defaultValue = "0") int page, @RequestParam(required = false,defaultValue = "10") int size) {
//        return myService.taxOutEntities(bin,PageRequest.of(page,size));
        return myService.pensionEntityUl(bin, year, PageRequest.of(page,size));
    }

    @GetMapping("/pensionsbyyear")
    public List<Map<String,Object>> pensionUl1(@RequestParam String bin, @RequestParam Double year, @RequestParam Integer page) {
//        return myService.taxOutEntities(bin,PageRequest.of(page,size));
        return myService.pensionEntityUl1(bin, year, page);
    }
    @GetMapping("/hierarchy")
    public FlRelativesLevelDto hierarchy(@RequestParam String iin) throws SQLException {
//        return myService.taxOutEntities(bin,PageRequest.of(page,size));
        return myService.createHierarchyObject(iin);
    }
    @GetMapping("/iin")
    public List<SearchResultModelFL> getByIIN(@RequestParam String iin, @RequestParam String email) throws IOException {
        List<SearchResultModelFL> fl = myService.getByIIN_photo(iin);
        log log = new log();
        log.setDate(LocalDateTime.now());
        log.setObwii("Искал в досье " + email + ": " + iin);
        log.setUsername(email);
        logRepo.save(log);
        return myService.getByIIN_photo(iin);
    }

    @GetMapping("/nomer_doc")
    public List<SearchResultModelFL> getByDoc(@RequestParam String doc) {
        return myService.getByDoc_photo(doc);
    }
    @GetMapping("/bydoc_number")
    public List<SearchResultModelFL> getByDocNumber(@RequestParam String doc_number) {
        return myService.getByDocNumber_photo(doc_number);
    }

    @GetMapping("/additionalfio")
    public List<SearchResultModelFL> getByAdditions(@RequestParam HashMap<String, String> req) {
        System.out.println(req);
        return myService.getWIthAddFields(req);
    }

    @GetMapping("/byphone")
    public List<SearchResultModelFL> getByPhone(@RequestParam String phone) {
        return myService.getByPhone(phone);
    }   @GetMapping("/byvinkuzov")
    public List<SearchResultModelFL> getByVinKuzov(@RequestParam String vin) {
        return myService.getByVinFl(vin.toUpperCase());
    }
    @GetMapping("/byvinkuzovul")
    public List<SearchResultModelUl> getByVinKuzovUl(@RequestParam String vin) {
        return myService.getByVinUl(vin.toUpperCase());
    }

    @GetMapping("/fio")
    public List<SearchResultModelFL> findByFIO(@RequestParam String i, @RequestParam String o, @RequestParam String f, @RequestParam String email) {
        log log = new log();
        log.setDate(LocalDateTime.now());
        log.setObwii("Искал в досье " + email + ": " + f + " " + i + " " + o);
        log.setUsername(email);
        logRepo.save(log);
        return myService.getByFIO_photo(i.replace('$', '%'), o.replace('$', '%'), f.replace('$', '%'));
    }

    @GetMapping("/bin")
    public List<SearchResultModelUl> findByBin(@RequestParam String bin, @RequestParam String email) {
        log log = new log();
        log.setDate(LocalDateTime.now());
        log.setObwii("Искал в досье " + email + ": " + bin);
        log.setUsername(email);
        logRepo.save(log);
        return myService.searchResultUl(bin);
    }

    @GetMapping("/binname")
    public List<SearchResultModelUl> findBinByName(@RequestParam String name) {
        return myService.searchUlByName(name.replace('$', '%'));
    }

    @GetMapping(value = "/download/{iin}", produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody byte[] generatePdfFile(HttpServletResponse response, @PathVariable("iin")String iin) throws IOException, DocumentException {
        response.setContentType("application/pdf");
        String headerkey = "Content-Disposition";
        String headervalue = "attachment; filename=doc" + ".pdf";
        response.setHeader(headerkey, headervalue);
        NodesFL r =  myService.getNode(iin);
        PdfGenerator generator = new PdfGenerator();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        generator.generate(r, baos);
        return baos.toByteArray();
    }

    @GetMapping(value = "/downloadbin/{bin}", produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody byte[] generateUlPdfFile(HttpServletResponse response, @PathVariable("bin")String bin) throws IOException, DocumentException {
        response.setContentType("application/pdf");
        String headerkey = "Content-Disposition";
        String headervalue = "attachment; filename=doc" + ".pdf";
        response.setHeader(headerkey, headervalue);
        NodesUL r =  myService.getNodeUL(bin);
        PdfGenerator generator = new PdfGenerator();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        generator.generate(r, baos);
        return baos.toByteArray();
    }
}