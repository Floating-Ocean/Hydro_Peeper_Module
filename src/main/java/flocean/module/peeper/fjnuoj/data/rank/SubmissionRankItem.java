package flocean.module.peeper.fjnuoj.data.rank;

import flocean.module.peeper.fjnuoj.data.UserData;

public record SubmissionRankItem(String rank, UserData user, int count, int maxCount) implements VisualRankItem {

    public SubmissionRankItem(SimpleRankItem rankItem, int maxCount) {
        this(rankItem.rank(), rankItem.who(), rankItem.val(), maxCount);
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
        return count() + "";
    }

    @Override
    public double getProgress() {
        return (double) count / maxCount;
    }
}
