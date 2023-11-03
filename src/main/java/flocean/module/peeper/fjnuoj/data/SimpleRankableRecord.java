package flocean.module.peeper.fjnuoj.data;

public record SimpleRankableRecord(UserData user, int count) implements RankableRecord {

    @Override
    public UserData fetchWho() {
        return new UserData(user().name().split(" \\(")[0], user().id());
    }

    @Override
    public int fetchCount() {
        return count();
    }
}
