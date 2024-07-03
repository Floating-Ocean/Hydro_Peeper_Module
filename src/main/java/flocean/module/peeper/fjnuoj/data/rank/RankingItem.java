package flocean.module.peeper.fjnuoj.data.rank;

import flocean.module.peeper.fjnuoj.data.UserData;

public record RankingItem(String rank, UserData user, int ac, int max) implements VisualRankItem {

    public RankingItem(SimpleRankItem rankItem, int max) {
        this(rankItem.rank(), rankItem.who(), rankItem.val(), max);
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
        return ac() + "题";
    }

    @Override
    public double getProgress() {
        return (double) ac / max;
    }
}
