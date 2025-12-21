package com.theatermgnt.theatermgnt.chatbotInternal.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SectionInfo {
   String sectionNumber;
   String sectionTitle;
   Integer startPosition;

   public String getFullTitle() {
      return sectionNumber + ". " + sectionTitle;
   }
}
