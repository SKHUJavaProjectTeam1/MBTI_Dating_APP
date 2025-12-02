package com.mbtidating.handler;

import com.mbtidating.config.JwtUtil;
import com.mbtidating.config.SpringConfigurator;
import com.mbtidating.dto.User;
import com.mbtidating.repository.UserRepository;
import com.mbtidating.service.MatchService;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@ServerEndpoint(
        value = "/ws/match/{token}",
        configurator = SpringConfigurator.class
)
public class MatchSocketHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchService matchService;

    // ============================================================
    // 1) 클라이언트 접속
    // ============================================================
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {

        String loginId = JwtUtil.validateToken(token); // username = loginId(userId)

        if (loginId == null) {
            session.close();
            return;
        }

        Optional<User> opt = userRepository.findByLoginId(loginId);
        if (opt.isEmpty()) {
            session.close();
            return;
        }

        User user = opt.get();

        boolean joined = matchService.join(user, session);
        if (!joined) {
            // 쿨타임 등으로 인해 큐 참여 거절
            session.close();
        }
    }

    // ============================================================
    // 2) 클라이언트 종료
    // ============================================================
    @OnClose
    public void onClose(Session session) {
        matchService.leave(session);
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        matchService.leave(session);
    }

    // 매칭용이라 메시지 수신은 별도 사용 안 함
    @OnMessage
    public void onMessage(Session session, String msg) {
        // 필요 없다면 비워둬도 됨
    }
}
