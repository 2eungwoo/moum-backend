package study.moum.auth.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberEntity is a Querydsl query type for MemberEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberEntity extends EntityPathBase<MemberEntity> {

    private static final long serialVersionUID = -1778365931L;

    public static final QMemberEntity memberEntity = new QMemberEntity("memberEntity");

    public final StringPath address = createString("address");

    public final StringPath email = createString("email");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath instrument = createString("instrument");

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final StringPath proficiency = createString("proficiency");

    public final StringPath profileDescription = createString("profileDescription");

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final ListPath<study.moum.record.domain.MemberRecordEntity, study.moum.record.domain.QMemberRecordEntity> records = this.<study.moum.record.domain.MemberRecordEntity, study.moum.record.domain.QMemberRecordEntity>createList("records", study.moum.record.domain.MemberRecordEntity.class, study.moum.record.domain.QMemberRecordEntity.class, PathInits.DIRECT2);

    public final StringPath role = createString("role");

    public final ListPath<study.moum.moum.team.domain.TeamMemberEntity, study.moum.moum.team.domain.QTeamMemberEntity> teams = this.<study.moum.moum.team.domain.TeamMemberEntity, study.moum.moum.team.domain.QTeamMemberEntity>createList("teams", study.moum.moum.team.domain.TeamMemberEntity.class, study.moum.moum.team.domain.QTeamMemberEntity.class, PathInits.DIRECT2);

    public final StringPath username = createString("username");

    public QMemberEntity(String variable) {
        super(MemberEntity.class, forVariable(variable));
    }

    public QMemberEntity(Path<? extends MemberEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMemberEntity(PathMetadata metadata) {
        super(MemberEntity.class, metadata);
    }

}

