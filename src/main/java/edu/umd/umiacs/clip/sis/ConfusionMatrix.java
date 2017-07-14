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

import static java.lang.Math.max;
import java.util.List;
import static java.util.stream.IntStream.range;

/**
 *
 * @author Mossaab Bagdouri
 */
public class ConfusionMatrix {

    public double TP, TN, FP, FN;

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

    @Override
    public String toString() {
        return "TP = " + TP + "\tTN = " + TN + "\tFP = " + FP + "\tFN = " + FN;
    }
}
