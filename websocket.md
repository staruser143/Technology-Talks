Let's break down the implementation with code snippets for both React.js (frontend) and NestJS (backend).


---

1. Frontend (React.js) Implementation

We'll use Socket.io for real-time updates.

Install Dependencies

npm install socket.io-client

Setup WebSocket Connection (socket.js)

import { io } from "socket.io-client";

const socket = io("http://localhost:3000"); // Connect to NestJS backend

export default socket;


---

Sidebar Component (Sidebar.js)

This component captures user answers and sends them to the backend.

import React, { useState } from "react";
import socket from "./socket";

const Sidebar = () => {
  const [responses, setResponses] = useState({});

  const questions = [
    { id: "q1", text: "Do you need maternity coverage?" },
    { id: "q2", text: "Do you prefer a lower premium or lower deductible?" },
  ];

  const handleAnswer = (questionId, answer) => {
    const updatedResponses = { ...responses, [questionId]: answer };
    setResponses(updatedResponses);

    // Emit event to backend
    socket.emit("question_answered", updatedResponses);
  };

  return (
    <div>
      <h2>Filter Plans</h2>
      {questions.map((q) => (
        <div key={q.id}>
          <p>{q.text}</p>
          <button onClick={() => handleAnswer(q.id, "Yes")}>Yes</button>
          <button onClick={() => handleAnswer(q.id, "No")}>No</button>
        </div>
      ))}
    </div>
  );
};

export default Sidebar;


---

Plan Listing Component (PlanListing.js)

This component listens for filtered plan updates from the backend.

import React, { useState, useEffect } from "react";
import socket from "./socket";

const PlanListing = () => {
  const [plans, setPlans] = useState([]);

  useEffect(() => {
    // Listen for filtered plans from backend
    socket.on("filtered_plans_received", (filteredPlans) => {
      setPlans(filteredPlans);
    });

    return () => {
      socket.off("filtered_plans_received");
    };
  }, []);

  return (
    <div>
      <h2>Available Plans</h2>
      <ul>
        {plans.map((plan, index) => (
          <li key={index}>{plan.name} - ${plan.premium}/month</li>
        ))}
      </ul>
    </div>
  );
};

export default PlanListing;


---

Main App Component (App.js)

Combines both components.

import React from "react";
import Sidebar from "./Sidebar";
import PlanListing from "./PlanListing";

const App = () => {
  return (
    <div style={{ display: "flex" }}>
      <div style={{ width: "30%" }}>
        <Sidebar />
      </div>
      <div style={{ width: "70%" }}>
        <PlanListing />
      </div>
    </div>
  );
};

export default App;


---

2. Backend (NestJS) Implementation

Install Dependencies

npm install @nestjs/websockets @nestjs/platform-socket.io socket.io


---

WebSocket Gateway (plan.gateway.ts)

This listens for user responses and sends filtered plans.

import {
  WebSocketGateway,
  SubscribeMessage,
  MessageBody,
  WebSocketServer,
} from "@nestjs/websockets";
import { Server } from "socket.io";
import { PlanService } from "./plan.service";

@WebSocketGateway({ cors: { origin: "*" } }) // Allow cross-origin requests
export class PlanGateway {
  @WebSocketServer()
  server: Server;

  constructor(private readonly planService: PlanService) {}

  @SubscribeMessage("question_answered")
  async handleUserResponse(@MessageBody() responses: Record<string, string>) {
    console.log("Received user responses:", responses);

    const filteredPlans = await this.planService.getFilteredPlans(responses);

    this.server.emit("filtered_plans_received", filteredPlans);
  }
}


---

Plan Service (plan.service.ts)

Implements filtering logic.

import { Injectable } from "@nestjs/common";

@Injectable()
export class PlanService {
  private plans = [
    { name: "Basic Health Plan", premium: 100, maternity: false },
    { name: "Premium Health Plan", premium: 300, maternity: true },
    { name: "Affordable Care Plan", premium: 150, maternity: false },
  ];

  getFilteredPlans(responses: Record<string, string>) {
    let filteredPlans = [...this.plans];

    if (responses["q1"] === "Yes") {
      filteredPlans = filteredPlans.filter((plan) => plan.maternity);
    }

    if (responses["q2"] === "Lower Premium") {
      filteredPlans = filteredPlans.sort((a, b) => a.premium - b.premium);
    }

    return filteredPlans;
  }
}


---

Module Setup (app.module.ts)

Register services and gateway.

import { Module } from "@nestjs/common";
import { PlanGateway } from "./plan.gateway";
import { PlanService } from "./plan.service";

@Module({
  providers: [PlanGateway, PlanService],
})
export class AppModule {}


---

Run the Backend

npm run start


---

3. Workflow

1. User selects an answer in the sidebar.


2. Frontend emits an event (question_answered).


3. Backend processes the event and filters the plans.


4. Filtered plans are sent back to the frontend (filtered_plans_received).


5. Frontend updates the plan listing dynamically.




---

4. Next Steps & Enhancements

Persist user selections using local storage or a database.

Use Kafka/RabbitMQ if real-time filtering should be scalable.

Optimize filtering logic by implementing caching (Redis).

Use OpenTelemetry tracing for debugging real-time events.


Would you like to extend this with a database for storing user selections?

