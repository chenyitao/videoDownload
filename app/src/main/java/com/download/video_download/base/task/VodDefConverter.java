/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.download.video_download.base.task;

import com.arialyy.aria.core.processor.IVodTsUrlConverter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class VodDefConverter implements IVodTsUrlConverter {
  @Override public List<String> convert(String m3u8Url, List<String> tsUrls) {
      List<String> convertedTsUrl = new ArrayList<>();
      if (tsUrls == null || tsUrls.isEmpty()) {
          return convertedTsUrl;
      }

      String baseUrl;
      try {
          URL url = new URL(m3u8Url);
          baseUrl = m3u8Url.substring(0, m3u8Url.lastIndexOf('/') + 1);
      } catch (Exception e) {
          int index = m3u8Url.lastIndexOf("/");
          baseUrl = index >= 0 ? m3u8Url.substring(0, index + 1) : m3u8Url + "/";
      }

      for (String tsUrl : tsUrls) {
          if (tsUrl == null || tsUrl.trim().isEmpty()) {
              continue;
          }

          if (tsUrl.startsWith("http://") || tsUrl.startsWith("https://")) {
              convertedTsUrl.add(tsUrl);
          }
          else if (tsUrl.startsWith("/")) {
              try {
                  URL url = new URL(m3u8Url);
                  String domainUrl = url.getProtocol() + "://" + url.getHost();
                  convertedTsUrl.add(domainUrl + tsUrl);
              } catch (Exception e) {
                  convertedTsUrl.add(baseUrl + tsUrl);
              }
          }
          else {
              convertedTsUrl.add(baseUrl + tsUrl);
          }
      }

      return convertedTsUrl;
  }
}
