package project.airbnb.clone.controller.chat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import project.airbnb.clone.WithMockGuest;
import project.airbnb.clone.controller.RestDocsTestSupport;
import project.airbnb.clone.dto.chat.ChatMessageResDto;
import project.airbnb.clone.dto.chat.ChatMessagesResDto;
import project.airbnb.clone.dto.chat.ChatRoomResDto;
import project.airbnb.clone.dto.chat.CreateChatRoomReqDto;
import project.airbnb.clone.dto.chat.LeaveChatRoomReqDto;
import project.airbnb.clone.dto.chat.UpdateChatRoomNameReqDto;
import project.airbnb.clone.service.chat.ChatService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest extends RestDocsTestSupport {

    private static final String CHAT_API_TAG = "Chat API";

    @MockitoBean ChatService chatService;

    @Test
    @DisplayName("새 채팅방 생성 또는 기존 채팅방 반환")
    @WithMockGuest
    void createOrGetChatRoom() throws Exception {
        //given
        CreateChatRoomReqDto request = new CreateChatRoomReqDto(1L);
        ChatRoomResDto response = new ChatRoomResDto(1L, "my-chat-room", 1L, "Ahmad Gul", "https://example.com", true,
                "안녕하세요", LocalDateTime.now().minusDays(7).truncatedTo(ChronoUnit.MICROS), 3);
        given(chatService.createOrGetChatRoom(anyLong(), anyLong())).willReturn(response);

        //when
        //then
        mockMvc.perform(post("/api/chat/rooms")
                       .header(AUTHORIZATION, "Bearer {access-token}")
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .content(creatJson(request))
               )
               .andExpectAll(
                       handler().handlerType(ChatController.class),
                       handler().methodName("createOrGetChatRoom"),
                       status().isOk(),
                       jsonPath("$.roomId").value(response.roomId()),
                       jsonPath("$.customRoomName").value(response.customRoomName()),
                       jsonPath("$.guestId").value(response.guestId()),
                       jsonPath("$.guestName").value(response.guestName()),
                       jsonPath("$.guestProfileImage").value(response.guestProfileImage()),
                       jsonPath("$.isOtherGuestActive").value(response.isOtherGuestActive()),
                       jsonPath("$.lastMessage").value(response.lastMessage()),
                       jsonPath("$.lastMessageTime").value(response.lastMessageTime().toString()),
                       jsonPath("$.unreadCount").value(response.unreadCount())
               )
               .andDo(document("create-or-get-chat-room",
                       resource(
                               builder()
                                       .tag(CHAT_API_TAG)
                                       .summary("새 채팅방 생성 or 기존 채팅방 반환")
                                       .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                       .requestFields(fieldWithPath("otherGuestId")
                                               .description("대화를 원하는 상대방 사용자 ID")
                                               .type(NUMBER)
                                       )
                                       .responseFields(
                                               fieldWithPath("roomId")
                                                       .type(NUMBER)
                                                       .description("채팅방 ID"),
                                               fieldWithPath("customRoomName")
                                                       .type(STRING)
                                                       .description("채팅방 이름 (초기에는 \"~님과의 대화\"로 생성)"),
                                               fieldWithPath("guestId")
                                                       .type(NUMBER)
                                                       .description("상대방 ID"),
                                               fieldWithPath("guestName")
                                                       .type(STRING)
                                                       .description("상대방 이름"),
                                               fieldWithPath("guestProfileImage")
                                                       .type(STRING)
                                                       .description("상대방 프로필 이미지 URL"),
                                               fieldWithPath("isOtherGuestActive")
                                                       .type(BOOLEAN)
                                                       .description("상대방 채팅방 나감 여부"),
                                               fieldWithPath("lastMessage")
                                                       .optional()
                                                       .type(STRING)
                                                       .description("마지막 메시지 내용 (기존 채팅방이 존재하는 경우)"),
                                               fieldWithPath("lastMessageTime")
                                                       .optional()
                                                       .type(STRING)
                                                       .description("마지막 메시지 전송 시간 (기존 채팅방이 존재하는 경우)"),
                                               fieldWithPath("unreadCount")
                                                       .type(NUMBER)
                                                       .description("읽지 않은 메시지 개수")
                                       )
                                       .requestSchema(schema("CreateChatRoomRequest"))
                                       .responseSchema(schema("ChatRoomResponse"))
                                       .build()
                       )
               ));
    }

    @Test
    @DisplayName("참여중인 전체 채팅방 조회")
    @WithMockGuest
    void getChatRooms() throws Exception {
        //given
        List<ChatRoomResDto> response = List.of(
                new ChatRoomResDto(1L, "my-chat-room-1", 1L, "Ahmad Gul", "https://example-a.com", true,
                        "안녕하세요", LocalDateTime.now().minusDays(7).truncatedTo(ChronoUnit.MICROS), 3),
                new ChatRoomResDto(2L, "my-chat-room-2", 2L, "Aleksey Begam", "https://example-b.com", false,
                        "반갑습니다", LocalDateTime.now().minusDays(6).truncatedTo(ChronoUnit.MICROS), 1),
                new ChatRoomResDto(3L, "my-chat-room-3", 3L, "Doris Sharma", "https://example-c.com", true,
                        "좋은 여행지입니다.", LocalDateTime.now().minusDays(5).truncatedTo(ChronoUnit.MICROS), 0)
        );

        given(chatService.getChatRooms(anyLong())).willReturn(response);

        //when
        //then
        mockMvc.perform(get("/api/chat/rooms")
                       .header(AUTHORIZATION, "Bearer {access-token}")
               )
               .andExpectAll(
                       handler().handlerType(ChatController.class),
                       handler().methodName("getChatRooms"),
                       status().isOk(),
                       jsonPath("$.length()").value(response.size())

               )
               .andDo(document("get-chat-rooms",
                       resource(
                               builder()
                                       .tag(CHAT_API_TAG)
                                       .summary("현재 참여중인 전체 채팅방 조회")
                                       .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                       .responseFields(
                                               fieldWithPath("[].roomId")
                                                       .type(NUMBER)
                                                       .description("채팅방 ID"),
                                               fieldWithPath("[].customRoomName")
                                                       .type(STRING)
                                                       .description("채팅방 이름"),
                                               fieldWithPath("[].guestId")
                                                       .type(NUMBER)
                                                       .description("상대방 ID"),
                                               fieldWithPath("[].guestName")
                                                       .type(STRING)
                                                       .description("상대방 이름"),
                                               fieldWithPath("[].guestProfileImage")
                                                       .type(STRING)
                                                       .description("상대방 프로필 이미지 URL"),
                                               fieldWithPath("[].isOtherGuestActive")
                                                       .type(BOOLEAN)
                                                       .description("상대방 채팅방 나감 여부"),
                                               fieldWithPath("[].lastMessage")
                                                       .optional()
                                                       .type(STRING)
                                                       .description("마지막 메시지 내용"),
                                               fieldWithPath("[].lastMessageTime")
                                                       .optional()
                                                       .type(STRING)
                                                       .description("마지막 메시지 전송 시간"),
                                               fieldWithPath("[].unreadCount")
                                                       .type(NUMBER)
                                                       .description("읽지 않은 메시지 개수")
                                       )
                                       .responseSchema(schema("ChatRoomResponse"))
                                       .build()
                       )
               ));
    }

    @Test
    @DisplayName("채팅방 메시지 기록 조회")
    @WithMockGuest
    void getMessageHistories() throws Exception {
        //given
        List<ChatMessageResDto> messages = List.of(
                new ChatMessageResDto(1L, 1L, 4L, "Maria Lai", "안녕하세요", LocalDateTime.now().minusDays(3)),
                new ChatMessageResDto(2L, 1L, 5L, "Ha Cui", "반갑습니다", LocalDateTime.now().minusDays(2)),
                new ChatMessageResDto(3L, 1L, 6L, "Halima Pham", "수고하세요", LocalDateTime.now().minusDays(1))
        );
        ChatMessagesResDto response = new ChatMessagesResDto(messages, true);

        given(chatService.getMessageHistories(anyLong(), anyLong(), anyInt(), anyLong())).willReturn(response);

        //when
        //then
        mockMvc.perform(get("/api/chat/{roomId}/messages", 1L)
                       .header(AUTHORIZATION, "Bearer {access-token}")
                       .param("lastMessageId", "3")
                       .param("size", "30")
               )
               .andExpectAll(
                       handler().handlerType(ChatController.class),
                       handler().methodName("getMessageHistories"),
                       status().isOk(),
                       jsonPath("$.messages.length()").value(response.messages().size()),
                       jsonPath("$.hasMore").value(response.hasMore())
               )
               .andDo(document("get-chat-messages",
                       resource(
                               builder()
                                       .tag(CHAT_API_TAG)
                                       .summary("특정 채팅방 메시지 기록 조회")
                                       .description("특정 채팅방의 메시지 기록을 스크롤 방식으로 조회합니다. 초기 lastMessageId를 전달하지 않으면 마지막 메시지부터 전달되므로 첫 요청 이후 마지막 메시지 ID를 전달하면 됩니다.")
                                       .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                       .pathParameters(parameterWithName("roomId").description("채팅방 ID"))
                                       .queryParameters(
                                               parameterWithName("lastMessageId")
                                                       .optional()
                                                       .description("마지막 메시지 ID"),
                                               parameterWithName("size")
                                                       .description("한번에 조회할 개수")
                                       )
                                       .responseFields(
                                               fieldWithPath("messages[].messageId")
                                                       .type(NUMBER)
                                                       .description("메시지 ID"),
                                               fieldWithPath("messages[].roomId")
                                                       .type(NUMBER)
                                                       .description("채팅방 ID"),
                                               fieldWithPath("messages[].senderId")
                                                       .type(NUMBER)
                                                       .description("전송자 ID"),
                                               fieldWithPath("messages[].senderName")
                                                       .type(STRING)
                                                       .description("전송자 이름"),
                                               fieldWithPath("messages[].content")
                                                       .type(STRING)
                                                       .description("메시지 내용"),
                                               fieldWithPath("messages[].timestamp")
                                                       .type(STRING)
                                                       .description("메시지 전송 시간"),
                                               fieldWithPath("hasMore")
                                                       .type(BOOLEAN)
                                                       .description("더 과거 메시지 기록 존재 여부")
                                       )
                                       .responseSchema(schema("ChatMessagesResponse"))
                                       .build()
                       )
               ));
    }

    @Test
    @DisplayName("채팅방 이름 설정")
    @WithMockGuest
    void updateChatRoomName() throws Exception {
        //given
        UpdateChatRoomNameReqDto request = new UpdateChatRoomNameReqDto("custom-room-name", 1L);
        ChatRoomResDto response = new ChatRoomResDto(1L, "custom-room-name", 1L, "Ahmad Gul", "https://example.com", true,
                "안녕하세요", LocalDateTime.now().minusDays(7).truncatedTo(ChronoUnit.MICROS), 3);

        given(chatService.updateChatRoomName(anyString(), anyLong(), anyLong(), anyLong())).willReturn(response);

        //when
        //then
        mockMvc.perform(patch("/api/chat/{roomId}/name", 1L)
                       .header(AUTHORIZATION, "Bearer {access-token}")
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .content(creatJson(request))
               )
               .andExpectAll(
                       handler().handlerType(ChatController.class),
                       handler().methodName("updateChatRoomName"),
                       status().isOk(),
                       jsonPath("$.roomId").value(response.roomId()),
                       jsonPath("$.customRoomName").value(response.customRoomName()),
                       jsonPath("$.guestId").value(response.guestId()),
                       jsonPath("$.guestName").value(response.guestName()),
                       jsonPath("$.guestProfileImage").value(response.guestProfileImage()),
                       jsonPath("$.isOtherGuestActive").value(response.isOtherGuestActive()),
                       jsonPath("$.lastMessage").value(response.lastMessage()),
                       jsonPath("$.lastMessageTime").value(response.lastMessageTime().toString()),
                       jsonPath("$.unreadCount").value(response.unreadCount())
               )
               .andDo(document("update-room-name",
                       resource(
                               builder()
                                       .tag(CHAT_API_TAG)
                                       .summary("채팅방 이름 수정")
                                       .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                       .pathParameters(parameterWithName("roomId").description("채팅방 ID"))
                                       .requestFields(
                                               fieldWithPath("customName")
                                                       .description("원하는 채팅방 이름")
                                                       .type(STRING),
                                               fieldWithPath("otherGuestId")
                                               .description("상대방 사용자 ID")
                                               .type(NUMBER)
                                       )
                                       .responseFields(
                                               fieldWithPath("roomId")
                                                       .type(NUMBER)
                                                       .description("채팅방 ID"),
                                               fieldWithPath("customRoomName")
                                                       .type(STRING)
                                                       .description("채팅방 이름"),
                                               fieldWithPath("guestId")
                                                       .type(NUMBER)
                                                       .description("상대방 ID"),
                                               fieldWithPath("guestName")
                                                       .type(STRING)
                                                       .description("상대방 이름"),
                                               fieldWithPath("guestProfileImage")
                                                       .type(STRING)
                                                       .description("상대방 프로필 이미지 URL"),
                                               fieldWithPath("isOtherGuestActive")
                                                       .type(BOOLEAN)
                                                       .description("상대방 채팅방 나감 여부"),
                                               fieldWithPath("lastMessage")
                                                       .optional()
                                                       .type(STRING)
                                                       .description("마지막 메시지 내용"),
                                               fieldWithPath("lastMessageTime")
                                                       .optional()
                                                       .type(STRING)
                                                       .description("마지막 메시지 전송 시간"),
                                               fieldWithPath("unreadCount")
                                                       .type(NUMBER)
                                                       .description("읽지 않은 메시지 개수")
                                       )
                                       .requestSchema(schema("UpdateChatRoomNameRequest"))
                                       .responseSchema(schema("ChatRoomResponse"))
                                       .build()
                       )
               ));
    }

    @Test
    @DisplayName("채팅방 나가기")
    @WithMockGuest
    void leaveChatRoom() throws Exception {
        //given
        LeaveChatRoomReqDto request = new LeaveChatRoomReqDto(true);

        //when
        //then
        mockMvc.perform(post("/api/chat/{roomId}", 1L)
                       .header(AUTHORIZATION, "Bearer {access-token}")
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .content(creatJson(request))
               )
               .andExpectAll(
                       handler().handlerType(ChatController.class),
                       handler().methodName("leaveChatRoom"),
                       status().isOk()
               )
               .andDo(document("leave-chat-room",
                       resource(
                               builder()
                                       .tag(CHAT_API_TAG)
                                       .summary("채팅방 나가기")
                                       .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                       .pathParameters(parameterWithName("roomId").description("채팅방 ID"))
                                       .requestFields(fieldWithPath("isActive").description("채팅방이 화면에서 활성화되어 있는지 여부").type(BOOLEAN))
                                       .requestSchema(schema("LeaveChatRoomRequest"))
                                       .build()
                       )
               ));
    }
}