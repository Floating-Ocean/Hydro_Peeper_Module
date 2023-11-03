package flocean.module.peeper.fjnuoj.data;

public record RankingData(String user, int ac, int id, int rank) implements RankableRecord {
    @Override
    public UserData fetchWho() {
        return new UserData(user.split(" \\(")[0], id);
    }

    @Override
    public int fetchCount() {
        return ac();
    }

    public UserData packUser(){
        return new UserData(user, id);
    }
}
