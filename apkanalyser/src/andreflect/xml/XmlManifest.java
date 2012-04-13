/*
 * Copyright (C) 2012 Sony Mobile Communications AB
 *
 * This file is part of ApkAnalyser.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package andreflect.xml;

public class XmlManifest {
    private String m_package = null;
    private String m_version = null;
    private String m_versionCode = null;
    private String m_mainActivity = null;

    public void setVersionCode(String vc) {
        m_versionCode = vc;
    }

    public String getVersionCode() {
        return m_versionCode;
    }

    public void setPackage(String pack) {
        m_package = pack;
    }

    public String getPackage() {
        return m_package;
    }

    public void setVersion(String ver) {
        m_version = ver;
    }

    public String getVersion() {
        return m_version;
    }

    public String getContextDescriptionName() {
        StringBuffer name = new StringBuffer();
        if (m_package != null) {
            name.append(m_package);
        }
        if (m_version != null)
        {
            name.append(" v" + m_version);
        } else if (m_versionCode != null) {
            name.append(" v" + m_versionCode);
        }

        return name.toString();
    }

    public void setMainActivity(String activity) {
        m_mainActivity = activity;
    }

    public String getMainActivity() {
        return m_mainActivity;
    }

    public String getLauncher() {
        if (m_mainActivity == null) {
            return null;
        }

        StringBuffer launcher = new StringBuffer();
        launcher.append(m_package);
        launcher.append("/");
        if (!m_mainActivity.startsWith(".")) {
            launcher.append(".");
        }
        launcher.append(m_mainActivity);
        return launcher.toString();
    }

}
