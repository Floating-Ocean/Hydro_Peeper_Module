package flocean.module.peeper.fjnuoj.data.rank;

public record SubmissionRankItem(String rank, String user, int count, int maxCount) implements VisualRankItem {

    public SubmissionRankItem(SimpleRankItem rankItem, int maxCount) {
        this(rankItem.rank(), rankItem.who(), rankItem.val(), maxCount);
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
        return count() + "";
    }

    @Override
    public double getProgress() {
        return (double) count / maxCount;
    }
}
