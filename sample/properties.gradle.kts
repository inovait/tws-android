/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
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
import java.util.Properties

val configProperties = loadProperties(projectDir.resolve("config.properties"))

// config.properties
extra["projectId"] = configProperties.getProperty("projectId")

fun loadProperties(file: File): Properties {
    val properties = Properties()
    if (file.exists()) {
        properties.load(file.inputStream())
    }
    return properties
}
