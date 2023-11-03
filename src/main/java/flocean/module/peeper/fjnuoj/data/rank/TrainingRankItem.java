package flocean.module.peeper.fjnuoj.data.rank;

import flocean.module.peeper.fjnuoj.data.UserData;

public record TrainingRankItem(String rank, UserData user, int proportion) implements VisualRankItem {

    public TrainingRankItem(SimpleRankItem rankItem) {
        this(rankItem.rank(), rankItem.who(), rankItem.val());
    }

    @Override
    public String fetchRank() {
        return rank();
    }

    @Override
    public UserData fetchWho() {
        return new UserData(user().name().split(" \\(")[0], user().id());
    }

    @Override
    public String fetchVal() {
        return proportion() + "%";
    }

    @Override
    public double getProgress() {
        return proportion / 100.0;
    }
}
