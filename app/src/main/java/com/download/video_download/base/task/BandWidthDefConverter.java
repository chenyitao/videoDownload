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

import com.arialyy.aria.core.processor.IBandWidthUrlConverter;

class BandWidthDefConverter implements IBandWidthUrlConverter {

  @Override public String convert(String m3u8Url, String bandWidthUrl) {
      if (bandWidthUrl.startsWith("/")) {
          int protocolIndex = m3u8Url.indexOf("://");
          int domainEndIndex = m3u8Url.indexOf("/", protocolIndex + 3);
          String domain = m3u8Url.substring(0, domainEndIndex);
          return domain + bandWidthUrl;
      }
      else {
          int index = m3u8Url.lastIndexOf("/");
          return m3u8Url.substring(0, index + 1) + bandWidthUrl;
      }
  }
}
