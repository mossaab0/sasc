/**
 * Tools Classifier
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
package edu.umd.umiacs.clip.sis;

import static edu.umd.umiacs.clip.tools.io.AllFiles.readAllLines;
import static java.lang.Math.max;
import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.List;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import org.apache.commons.lang3.tuple.Triple;

/**
 *
 * @author Mossaab Bagdouri
 */
public class ConfusionMatrix {

    private static final double N_TOTAL = Integer.MAX_VALUE;
    public double TP, TN, FP, FN;
    private ConfusionMatrix[] sample;

    public ConfusionMatrix() {
    }

    public ConfusionMatrix(double TP, double TN, double FP, double FN) {
        this.TP = TP;
        this.TN = TN;
        this.FP = FP;
        this.FN = FN;
    }

    public ConfusionMatrix(List<Boolean> gold, List<Boolean> predictions) {
        int total = max(gold.size(), predictions.size());
        TP = (int) range(0, total).filter(i -> gold.get(i) && predictions.get(i)).count();
        FP = (int) range(0, total).filter(i -> !gold.get(i) && predictions.get(i)).count();
        FN = (int) range(0, total).filter(i -> gold.get(i) && !predictions.get(i)).count();
        TN = total - (TP + FP + FN);
    }

    public float getRecall() {
        return (float) (TP / (TP + FN));
    }

    public float getPrecision() {
        return (float) (TP / (TP + FP));
    }

    public float getF1() {
        return (float) (2 * TP / (2f * TP + FN + FP));
    }

    public float getAccuracy() {
        return (float) ((TP + TN) / (float) (TP + TN + FP + FN));
    }

    public static ConfusionMatrix loadLibSVM(String goldPath, String predPath, double... cutoffs) {
        int[] gold = readAllLines(goldPath).stream().
                mapToInt(line -> new Integer(line.split("\\s+")[0]))
                .toArray();
        IntSummaryStatistics stats = Arrays.stream(gold).summaryStatistics();
        double cutoff = stats.getMin() == stats.getMax() ? cutoffs[0] : ((stats.getMax() + stats.getMin()) / 2);
        List<Boolean> goldList = Arrays.stream(gold).boxed().map(i -> i > cutoff).collect(toList());
        List<Boolean> predList = readAllLines(predPath).stream().map(pred -> new Double(pred) > cutoff).collect(toList());
        return new ConfusionMatrix(goldList, predList);
    }

    public Triple<Float, Float, Float> getF1withCI() {
        double n[] = new double[]{FN + TN, TP + FP};
        double r[] = new double[]{FN, TP};
        double N[] = range(0, 2).mapToDouble(i -> n[i] * N_TOTAL / (n[0] + n[1])).toArray();
        double R[] = range(0, 2).mapToDouble(i -> r[i] * N_TOTAL / (n[0] + n[1])).toArray();
        double Var_R[] = range(0, 2).
                mapToDouble(i -> Math.pow(N[i], 2) * r[i] * (1 - r[i] / n[i])
                        / Math.pow(n[i], 2)).toArray();
        double temp = 2 * R[1] / Math.pow(R[1] + R[0] + N[1], 2);
        double Var_F1_1 = Math.pow(2 / (R[1] + R[0] + N[1]) - temp, 2);
        double Var_F1_0 = Math.pow(temp, 2);
        double Var_F1 = Var_F1_1 * Var_R[1] + Var_F1_0 * Var_R[0];
        float pe = getF1();
        float delta = (float) (1.96 * Math.sqrt(Var_F1));
        return Triple.of(pe - delta, pe, pe + delta);
    }

    @Override
    public String toString() {
        return "TP = " + TP + "\tTN = " + TN + "\tFP = " + FP + "\tFN = " + FN;
    }
}
