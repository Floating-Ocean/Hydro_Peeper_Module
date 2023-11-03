package flocean.module.peeper.fjnuoj.data;

import flocean.module.peeper.fjnuoj.enums.VerdictType;

public record SubmissionData(UserData user, int score, VerdictType verdictType, String problemName, long at) {
}
