package com.theatermgnt.theatermgnt.chatbotInternal.util;

import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.SectionInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DocumentSectionTracker {
    private final List<SectionInfo> sections = new ArrayList<>();

    private static final Pattern CHAPTER_PATTERN = Pattern.compile(
            "^\\s*(CHƯƠNG\\s+[IVXLCDM]+)[:\\s]+(.{3,150})$",
            Pattern.MULTILINE | Pattern.UNICODE_CASE
    );

    private static final Pattern SUBSECTION_PATTERN = Pattern.compile(
            "^\\s*(\\d+\\.\\d+)\\.?\\s+([A-Za-zÀ-ỹĐđ][^\\n\\r]{2,150})$",
            Pattern.MULTILINE | Pattern.UNICODE_CASE
    );

    private static final Pattern SIMPLE_SECTION_PATTERN = Pattern.compile(
            "^\\s*(\\d+)\\.\\s+([A-Za-zÀ-ỹĐđ][^\\n\\r]{2,150})$",
            Pattern.MULTILINE | Pattern.UNICODE_CASE
    );

    public DocumentSectionTracker(String fullText){
        if(fullText != null && !fullText.isEmpty()){
            parseDocument(fullText);
        }
    }


    public SectionInfo getSectionFromText(String chunkText) {
        if (sections.isEmpty() || chunkText == null || chunkText.isEmpty()) {
            return null;
        }
        
        for (SectionInfo section : sections) {
            // Escape special regex characters trong section number
            String escapedNumber = Pattern.quote(section.getSectionNumber());
            String escapedTitle = Pattern.quote(section.getSectionTitle());
            
            String sectionPattern = escapedNumber + "[:\\s\\.]+.*?" +
                escapedTitle.substring(0, Math.min(20, escapedTitle.length()));
            
            try {
                Pattern pattern = Pattern.compile(sectionPattern, Pattern.CASE_INSENSITIVE);
                if (pattern.matcher(chunkText).find()) {
                    return section;
                }
            } catch (Exception e) {
                // Fallback: simple contains check
                if (chunkText.contains(section.getSectionNumber()) && 
                    chunkText.contains(section.getSectionTitle().substring(0, Math.min(15, section.getSectionTitle().length())))) {
                    return section;
                }
            }
        }
        
        return null;
    }
    
    public List<SectionInfo> getSections(){
        return new ArrayList<>(sections);
    }
    public boolean hasSections(){
        return !sections.isEmpty();
    }


    private void parseDocument(String fullText){
        String preview = fullText.length() > 500 ? fullText.substring(0, 500) : fullText;
        log.debug("Document preview (first 500 chars):\n{}", preview);
        
        Matcher chapterMatcher = CHAPTER_PATTERN.matcher(fullText);
        while(chapterMatcher.find()){
            int startPos = chapterMatcher.start();
            String sectionNumber = chapterMatcher.group(1).trim();
            String sectionTitle = chapterMatcher.group(2).trim();
            
            log.debug("Found chapter: {} - {}", sectionNumber, sectionTitle);
            sections.add(SectionInfo.builder()
                    .sectionNumber(sectionNumber)
                    .sectionTitle(sectionTitle)
                    .startPosition(startPos)
                    .build());
        }

        // Parse subsections (1.1., 1.2., etc.)
        Matcher subMatcher = SUBSECTION_PATTERN.matcher(fullText);
        while(subMatcher.find()){
            int startPos = subMatcher.start();
            String sectionNumber = subMatcher.group(1).trim();
            String sectionTitle = subMatcher.group(2).trim();
            
            log.debug("Found subsection: {} - {}", sectionNumber, sectionTitle);
            sections.add(SectionInfo.builder()
                    .sectionNumber(sectionNumber)
                    .sectionTitle(sectionTitle)
                    .startPosition(startPos)
                    .build());
        }

        // Fallback: Parse simple sections (1., 2., etc.)
        if (sections.isEmpty()) {
            Matcher simpleMatcher = SIMPLE_SECTION_PATTERN.matcher(fullText);
            while(simpleMatcher.find()){
                int startPos = simpleMatcher.start();
                String sectionNumber = simpleMatcher.group(1).trim();
                String sectionTitle = simpleMatcher.group(2).trim();
                
                log.debug("Found simple section: {} - {}", sectionNumber, sectionTitle);
                sections.add(SectionInfo.builder()
                        .sectionNumber(sectionNumber)
                        .sectionTitle(sectionTitle)
                        .startPosition(startPos)
                        .build());
            }
        }

        sections.sort(Comparator.comparingInt(SectionInfo::getStartPosition));
        log.info("Parsed {} sections from document ({} chars)", sections.size(), fullText.length());
    }
}




