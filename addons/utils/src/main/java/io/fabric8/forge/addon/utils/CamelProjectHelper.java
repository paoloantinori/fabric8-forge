/**
 *  Copyright 2005-2015 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.fabric8.forge.addon.utils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import io.fabric8.utils.IOHelpers;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.DependencyFacet;

public class CamelProjectHelper {

    public static Dependency findCamelCoreDependency(Project project) {
        return findCamelArtifactDependency(project, "camel-core");
    }

    public static boolean hasFunktionDependency(Project project) {
        return hasDependency(project, "io.fabric8.funktion", "funktion-runtime");
    }

    public static Dependency findCamelSpringDependency(Project project) {
        return findCamelArtifactDependency(project, "camel-spring");
    }

    public static Dependency findCamelSpringBootDependency(Project project) {
        return findCamelArtifactDependency(project, "camel-spring-boot");
    }

    public static Dependency findCamelCDIDependency(Project project) {
        return findCamelArtifactDependency(project, "camel-cdi");
    }

    public static Dependency findCamelBlueprintDependency(Project project) {
        return findCamelArtifactDependency(project, "camel-blueprint");
    }

    public static Dependency findCamelArtifactDependency(Project project, String artifactId) {
        List<Dependency> dependencies = project.getFacet(DependencyFacet.class).getEffectiveDependencies();
        for (Dependency d : dependencies) {
            if ("org.apache.camel".equals(d.getCoordinate().getGroupId()) && artifactId.equals(d.getCoordinate().getArtifactId())) {
                return d;
            }
        }
        return null;
    }

    public static Set<Dependency> findCamelArtifacts(Project project) {
        Set<Dependency> answer = new LinkedHashSet<Dependency>();

        List<Dependency> dependencies = project.getFacet(DependencyFacet.class).getEffectiveDependencies();
        for (Dependency d : dependencies) {
            if ("org.apache.camel".equals(d.getCoordinate().getGroupId())) {
                answer.add(d);
            }
        }
        return answer;
    }

    public static Set<Dependency> findCustomCamelArtifacts(Project project) {
        Set<Dependency> answer = new LinkedHashSet<Dependency>();

        List<Dependency> dependencies = project.getFacet(DependencyFacet.class).getEffectiveDependencies();
        for (Dependency d : dependencies) {
            if (isCamelComponentArtifact(d)) {
                answer.add(d);
            }
        }
        return answer;
    }

    public static boolean isCamelComponentArtifact(Dependency dependency) {
        try {
            // is it a JAR file
            File file = dependency.getArtifact().getUnderlyingResourceObject();
            if (file != null && file.getName().toLowerCase().endsWith(".jar")) {
                URL url = new URL("file:" + file.getAbsolutePath());
                URLClassLoader child = new URLClassLoader(new URL[]{url});

                // custom component
                InputStream is = child.getResourceAsStream("META-INF/services/org/apache/camel/component.properties");
                if (is != null) {
                    IOHelpers.close(is);
                    return true;
                }
            }
        } catch (Throwable e) {
            // ignore
        }

        return false;
    }

    public static boolean hasDependency(Project project, String groupId) {
        return hasDependency(project, groupId, null, null) ||
                hasManagedDependency(project, groupId, null, null);
    }

    public static boolean hasDependency(Project project, String groupId, String artifactId) {
        return hasDependency(project, groupId, artifactId, null) ||
                hasManagedDependency(project, groupId, artifactId, null);
    }

    public static boolean hasDependency(Project project, String groupId, String artifactId, String version) {
        List<Dependency> dependencies = project.getFacet(DependencyFacet.class).getEffectiveDependencies();
        for (Dependency d : dependencies) {
            boolean match = d.getCoordinate().getGroupId().equals(groupId);
            if (match && artifactId != null) {
                match = d.getCoordinate().getArtifactId().equals(artifactId);
            }
            if (match && version != null) {
                match = d.getCoordinate().getVersion().equals(version);
            }
            if (match) {
                return match;
            }
        }
        return false;
    }

    public static boolean hasManagedDependency(Project project, String groupId, String artifactId, String version) {
        List<Dependency> dependencies = project.getFacet(DependencyFacet.class).getManagedDependencies();
        for (Dependency d : dependencies) {
            boolean match = d.getCoordinate().getGroupId().equals(groupId);
            if (match && artifactId != null) {
                match = d.getCoordinate().getArtifactId().equals(artifactId);
            }
            if (match && version != null) {
                match = d.getCoordinate().getVersion().equals(version);
            }
            if (match) {
                return match;
            }
        }
        return false;
    }

}
