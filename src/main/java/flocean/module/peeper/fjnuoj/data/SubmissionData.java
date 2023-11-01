package flocean.module.peeper.fjnuoj.data;

import flocean.module.peeper.fjnuoj.enums.VerdictType;

public record SubmissionData(String user, int id, int score, VerdictType verdictType, String problemName, long at) {
}
