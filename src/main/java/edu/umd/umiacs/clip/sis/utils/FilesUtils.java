/**
 * Tools IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.umd.umiacs.clip.sis.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import static java.nio.charset.CodingErrorAction.IGNORE;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 *
 * @author Mossaab Bagdouri
 */
public class FilesUtils {

    public static final int BUFFER_SIZE = 1024 * 1024;
    public static final boolean REMOVE_OLD_FILE = true;

    private static List<String> readAllLines(Path path) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(newInputStream(path), BUFFER_SIZE), UTF_8.newDecoder().onMalformedInput(IGNORE)))) {
            List<String> result = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<String> readAllLines(File file) {
        return readAllLines(file.toPath());
    }

    private static String format(String path) {
        if (path.startsWith("~" + File.separator)) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }

    public static List<String> readAllLines(String path) {
        File file = new File(format(path));
        if (!path.contains("*")) {
            return readAllLines(file);
        }
        List<String> lines = new ArrayList();
        Stream.of(file.getParentFile()
                .listFiles((dir, name) -> name.matches(file.getName().replace(".", "\\.").replace("*", ".+"))))
                .sorted()
                .map(FilesUtils::readAllLines)
                .flatMap(Collection::stream)
                .forEach(lines::add);
        return lines;
    }

    public static List<String> readAllLinesFromResource(String path) {
        path = format(path);
        return readAllLines(System.class.getResourceAsStream(path));
    }

    public static List<String> readAllLines(InputStream is) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader bis = new BufferedReader(new InputStreamReader(is, UTF_8.newDecoder().onMalformedInput(IGNORE)))) {
            String line;
            while ((line = bis.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static Stream<String> lines(InputStream is) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, UTF_8.newDecoder().onMalformedInput(IGNORE)));
        return br.lines().onClose(asUncheckedRunnable(br));
    }

    protected static Runnable asUncheckedRunnable(Closeable c) {
        return () -> {
            try {
                c.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    public static Stream<File> list(Path dir) {
        try {
            return Files.list(dir).map(Path::toFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Stream<File> list(File dir) {
        return list(dir.toPath());
    }

    public static Stream<File> list(String dir) {
        return list(new File(format(dir)));
    }

    public static Stream<String> lines() {
        BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(System.in, BUFFER_SIZE), UTF_8.newDecoder().onMalformedInput(IGNORE)));
        return br.lines().onClose(asUncheckedRunnable(br));
    }

    public static Stream<String> lines(Path path) {
        try {
            String p = path.toString();
            if (!p.contains("*")) {
                return overridenLines(path);
            } else {
                File file = path.toFile();
                return Stream.of(file.getParentFile()
                        .listFiles((dir, name) -> name.matches(file.getName().replace(".", "\\.").replace("*", ".+"))))
                        .sorted()
                        .flatMap(FilesUtils::lines);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Stream<String> overridenLines(Path path) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new BufferedInputStream(newInputStream(path), BUFFER_SIZE), UTF_8.newDecoder().onMalformedInput(IGNORE)));
            return br.lines().onClose(asUncheckedRunnable(br));
        } catch (IOException e) {
            try {
                br.close();
            } catch (Exception ex) {
                try {
                    e.addSuppressed(ex);
                } catch (Throwable ignore) {
                }
            }
            throw e;
        }
    }

    public static Stream<String> lines(File file) {
        return lines(file.toPath());
    }

    public static Stream<String> lines(String path) {
        return lines(new File(format(path)));
    }

    public static Path write(Path path, Stream<?> lines, Charset cs, OpenOption... options) {
        if (options.length == 0) {
            options = new OpenOption[]{CREATE_NEW};
        }
        Objects.requireNonNull(lines);
        CharsetEncoder encoder = cs.newEncoder();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(newOutputStream(path, options), encoder), BUFFER_SIZE)) {
            lines.forEach(line -> {
                try {
                    writer.append(line.toString());
                    writer.newLine();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return path;
    }

    public static Path write(Path path, Stream<?> lines, OpenOption... options) {
        return write(path, lines, UTF_8, options);
    }

    public static Path write(String path, Iterable<? extends CharSequence> lines, OpenOption... options) {
        return write(new File(format(path)), lines, options);
    }

    public static Path write(String path, Iterable<? extends CharSequence> lines, boolean removeOldFile, OpenOption... options) {
        path = format(path);
        if (removeOldFile) {
            new File(path).delete();
        }
        return write(new File(path), lines, options);
    }

    public static Path write(String path, Stream<?> lines, OpenOption... options) {
        return write(new File(format(path)), lines, options);
    }

    public static Path write(String path, Stream<?> lines, boolean removeOldFile, OpenOption... options) {
        path = format(path);
        if (removeOldFile) {
            new File(path).delete();
        }
        return write(new File(path), lines, options);
    }

    public static Path write(File file, Iterable<? extends CharSequence> lines, OpenOption... options) {
        return write(file.toPath(), lines, options);
    }

    public static Path write(File file, Stream<?> lines, OpenOption... options) {
        return write(file.toPath(), lines, options);
    }

    public static Path write(Path path, Iterable<? extends CharSequence> lines, OpenOption... options) {
        if (options.length == 0) {
            options = new OpenOption[]{CREATE_NEW};
        }
        try {
            File parent = path.getParent().toFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            return Files.write(path, lines, UTF_8, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
