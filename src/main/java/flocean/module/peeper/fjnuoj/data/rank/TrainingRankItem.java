package flocean.module.peeper.fjnuoj.data.rank;

public record TrainingRankItem(String rank, String user, int proportion) implements VisualRankItem {

    public TrainingRankItem(SimpleRankItem rankItem) {
        this(rankItem.rank(), rankItem.who(), rankItem.val());
    }

    @Override
    public String fetchRank() {
        return rank();
    }

    @Override
    public String fetchWho() {
        return user().split(" \\(")[0];
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
