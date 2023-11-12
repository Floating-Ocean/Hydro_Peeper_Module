package flocean.module.peeper.fjnuoj.data.rank;

import flocean.module.peeper.fjnuoj.data.UserData;

public interface VisualRankItem {
    String fetchRank();

    UserData fetchWho();

    String fetchVal();

    double getProgress();
}
